package org.example.app.dwm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.example.app.func.DimAsyncFunction;
import org.example.bean.OrderDetail;
import org.example.bean.OrderInfo;
import org.example.bean.OrderWide;
import org.example.utils.MyKafkaUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class OrderWideApp {
    public static void main(String[] args) throws Exception {
        //1.获取执行环境
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        //1.1 设置状态后端
        //env.setStateBackend(new FsStateBackend("hdfs://hadoop100:8020/gmall/dwd_log/ck"));
        // //1.2 开启 CK
       // env.enableCheckpointing(10000L, CheckpointingMode.EXACTLY_ONCE);
        //env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.读取 Kafka 订单和订单明细主题数据 dwd_order_info dwd_order_detail
        String orderInfoSourceTopic = "dwd_order_info";
        String orderDetailSourceTopic = "dwd_order_detail";
        String orderWideSinkTopic = "dwm_order_wide";
        String groupId = "order_wide_group";
        FlinkKafkaConsumer<String> orderInfoKafkaSource =
                MyKafkaUtil.getKafkaSource(orderInfoSourceTopic, groupId);
        DataStreamSource<String> orderInfoKafkaDS = env.addSource(orderInfoKafkaSource);
        FlinkKafkaConsumer<String> orderDetailKafkaSource =
                MyKafkaUtil.getKafkaSource(orderDetailSourceTopic, groupId);
        DataStreamSource<String> orderDetailKafkaDS =
                env.addSource(orderDetailKafkaSource);
        //3.将每行数据转换为 JavaBean,提取时间戳生成 WaterMark
        WatermarkStrategy<OrderInfo> orderInfoWatermarkStrategy =
                WatermarkStrategy.<OrderInfo>forMonotonousTimestamps()
                        .withTimestampAssigner(new SerializableTimestampAssigner<OrderInfo>() {
                            @Override
                            public long extractTimestamp(OrderInfo element, long recordTimestamp) {
                                return element.getCreate_ts();
                            }
                        });
        WatermarkStrategy<OrderDetail> orderDetailWatermarkStrategy =
                WatermarkStrategy.<OrderDetail>forMonotonousTimestamps()
                        .withTimestampAssigner(new SerializableTimestampAssigner<OrderDetail>() {
                            @Override
                            public long extractTimestamp(OrderDetail element, long recordTimestamp) {
                                return element.getCreate_ts();
                            }

                        });
        KeyedStream<OrderInfo, Long> orderInfoWithIdKeyedStream = orderInfoKafkaDS.map(jsonStr ->
                {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //将 JSON 字符串转换为 JavaBean
                    OrderInfo orderInfo = JSON.parseObject(jsonStr, OrderInfo.class);
                    //取出创建时间字段
                    String create_time = orderInfo.getCreate_time();
                    //按照空格分割
                    String[] createTimeArr = create_time.split(" ");
                    orderInfo.setCreate_date(createTimeArr[0]);
                    orderInfo.setCreate_hour(createTimeArr[1]);
                    orderInfo.setCreate_ts(sdf.parse(create_time).getTime());
                    return orderInfo;
                }).assignTimestampsAndWatermarks(orderInfoWatermarkStrategy)
                .keyBy(OrderInfo::getId);
        KeyedStream<OrderDetail, Long> orderDetailWithOrderIdKeyedStream =
                orderDetailKafkaDS.map(jsonStr -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            OrderDetail orderDetail = JSON.parseObject(jsonStr, OrderDetail.class);
                            orderDetail.setCreate_ts(sdf.parse(orderDetail.getCreate_time()).getTime());
                            return orderDetail;
                        }).assignTimestampsAndWatermarks(orderDetailWatermarkStrategy)
                        .keyBy(OrderDetail::getOrder_id);
        //4.双流 JOIN
        SingleOutputStreamOperator<OrderWide> orderWideDS =
                orderInfoWithIdKeyedStream.intervalJoin(orderDetailWithOrderIdKeyedStream)
                        .between(Time.seconds(-5), Time.seconds(5)) //生产环境,为了不丢数据,设置时间为最大网络延迟
                        .process(new ProcessJoinFunction<OrderInfo, OrderDetail, OrderWide>() {
                            @Override
                            public void processElement(OrderInfo orderInfo, OrderDetail orderDetail, Context
                                    context, Collector<OrderWide> collector) throws Exception {
                                collector.collect(new OrderWide(orderInfo, orderDetail));
                            }
                        });
        //
        //orderWideDS.print("orderWideDS====");
        //5.关联维度
        //5.1 关联用户维度
        SingleOutputStreamOperator<OrderWide> orderWideWithUserDS = AsyncDataStream.unorderedWait(orderWideDS,
                new DimAsyncFunction<OrderWide>("DIM_USER_INFO") {
                    @Override
                    public String getKey(OrderWide orderWide) {
                        return orderWide.getUser_id().toString();
                    }

                    @Override
                    public void join(OrderWide orderWide, JSONObject dimInfo) throws
                            ParseException {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        //取出用户维度中的生日
                        String birthday = dimInfo.getString("BIRTHDAY");
                        long currentTS = System.currentTimeMillis();
                        Long ts = sdf.parse(birthday).getTime();
                        //将生日字段处理成年纪
                        Long ageLong = (currentTS - ts) / 1000L / 60 / 60 / 24 / 365;
                        orderWide.setUser_age(ageLong.intValue());
                        //取出用户维度中的性别
                        String gender = dimInfo.getString("GENDER");
                        orderWide.setUser_gender(gender);
                    }
                },
                60,
                TimeUnit.SECONDS);
        //  orderWideWithUserDS.print("orderWideWithUserDS==>");
        //5.2 关联地区维度
        SingleOutputStreamOperator<OrderWide> orderWideWithProvinceDS =
                AsyncDataStream.unorderedWait(orderWideWithUserDS,
                        new DimAsyncFunction<OrderWide>("DIM_BASE_PROVINCE") {
                            @Override
                            public String getKey(OrderWide orderWide) {
                                return orderWide.getProvince_id().toString();
                            }

                            @Override
                            public void join(OrderWide orderWide, JSONObject dimInfo) throws
                                    Exception {
                                //提取维度信息并设置进 orderWide
                                orderWide.setProvince_name(dimInfo.getString("NAME"));

                                orderWide.setProvince_area_code(dimInfo.getString("AREA_CODE"));

                                orderWide.setProvince_iso_code(dimInfo.getString("ISO_CODE"));

                                orderWide.setProvince_3166_2_code(dimInfo.getString("ISO_3166_2"));
                            }
                        }, 60, TimeUnit.SECONDS);
        SingleOutputStreamOperator<OrderWide> orderWideWithSkuDS =
                AsyncDataStream.unorderedWait(
                        orderWideWithProvinceDS, new DimAsyncFunction<OrderWide>("DIM_SKU_INFO") {
                            @Override
                            public void join(OrderWide orderWide, JSONObject jsonObject) throws Exception {
                                orderWide.setSku_name(jsonObject.getString("SKU_NAME"));
                                orderWide.setCategory3_id(jsonObject.getLong("CATEGORY3_ID"));
                                orderWide.setSpu_id(jsonObject.getLong("SPU_ID"));
                                orderWide.setTm_id(jsonObject.getLong("TM_ID"));
                            }

                            @Override
                            public String getKey(OrderWide orderWide) {
                                return String.valueOf(orderWide.getSku_id());
                            }
                        }, 60, TimeUnit.SECONDS);
        SingleOutputStreamOperator<OrderWide> orderWideWithSpuDS =
                AsyncDataStream.unorderedWait(
                        orderWideWithSkuDS, new DimAsyncFunction<OrderWide>("DIM_SPU_INFO") {
                            @Override
                            public void join(OrderWide orderWide, JSONObject jsonObject) throws Exception {
                                orderWide.setSpu_name(jsonObject.getString("SPU_NAME"));
                            }

                            @Override
                            public String getKey(OrderWide orderWide) {
                                return String.valueOf(orderWide.getSpu_id());
                            }
                        }, 60, TimeUnit.SECONDS);
        SingleOutputStreamOperator<OrderWide> orderWideWithTmDS =
                AsyncDataStream.unorderedWait(
                        orderWideWithSpuDS, new DimAsyncFunction<OrderWide>("DIM_BASE_TRADEMARK") {
                            @Override
                            public void join(OrderWide orderWide, JSONObject jsonObject) throws Exception {
                                orderWide.setTm_name(jsonObject.getString("TM_NAME"));
                            }

                            @Override
                            public String getKey(OrderWide orderWide) {
                                return String.valueOf(orderWide.getTm_id());
                            }
                        }, 60, TimeUnit.SECONDS);
        //5.6 关联品类维度
        SingleOutputStreamOperator<OrderWide> orderWideWithCategory3DS =
                AsyncDataStream.unorderedWait(
                        orderWideWithTmDS, new DimAsyncFunction<OrderWide>("DIM_BASE_CATEGORY3") {
                            @Override
                            public void join(OrderWide orderWide, JSONObject jsonObject) throws Exception {
                                orderWide.setCategory3_name(jsonObject.getString("NAME"));
                            }

                            @Override
                            public String getKey(OrderWide orderWide) {
                                return String.valueOf(orderWide.getCategory3_id());
                            }
                        }, 60, TimeUnit.SECONDS);
        orderWideWithCategory3DS.print("orderWideWithCategory3DS++++");
        //6.写入数据到 Kafka dwm_order_wide
        orderWideWithCategory3DS.map(JSON::toJSONString)
                .addSink(MyKafkaUtil.getKafkaSink(orderWideSinkTopic));
// orderWideWithProvinceDS.print();
        //7.开启任务
        env.execute();
    }
}
