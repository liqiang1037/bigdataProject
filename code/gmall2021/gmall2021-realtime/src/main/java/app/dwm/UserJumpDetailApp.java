package app.dwm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;

import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.PatternTimeoutFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import utils.MyKafkaUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;


public class UserJumpDetailApp {
    public static void main(String[] args) throws Exception {
        //1.获取执行环境
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //1.1 设置状态后端
        //env.setStateBackend(new
//FsStateBackend("hdfs://hadoop102:8020/gmall/dwd_log/ck"));
        //1.2 开启 CK
        //env.enableCheckpointing(10000L, CheckpointingMode.EXACTLY_ONCE);
        //env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.读取 Kafka dwd_page_log 主题数据创建流
        String sourceTopic = "dwd_page_log";
        String groupId = "userJumpDetailApp";
        String sinkTopic = "dwm_user_jump_detail";
        FlinkKafkaConsumer<String> kafkaSource = MyKafkaUtil.getKafkaSource(sourceTopic,
                groupId);
        DataStreamSource<String> kafkaDS = env.addSource(kafkaSource);
// DataStream<String> kafkaDS = env.socketTextStream("hadoop102", 9999);
        //提取数据中的时间戳生成 Watermark
        //老版本,默认使用的处理时间语义,新版本默认时间语义为事件时间
// env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //3.将数据转换为 JSON 对象
        WatermarkStrategy<JSONObject> watermarkStrategy = WatermarkStrategy
                .<JSONObject>forMonotonousTimestamps()
                .withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
                    @Override
                    public long extractTimestamp(JSONObject element, long recordTimestamp) {
                        return element.getLong("ts");
                    }
                });
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.process(
                new ProcessFunction<String, JSONObject>() {
                    @Override
                    public void processElement(String s, Context context, Collector<JSONObject>
                            collector) throws Exception {
                        try {
                            JSONObject jsonObject = JSON.parseObject(s);
                            collector.collect(jsonObject);
                        } catch (Exception e) {
                            context.output(new OutputTag<String>("dirty") {
                            }, s);
                        }
                    }
                }).assignTimestampsAndWatermarks(watermarkStrategy);
        jsonObjDS.print(">>>>>>>>>>>");
        //4.按照 Mid 进行分区
        KeyedStream<JSONObject, String> keyedStream = jsonObjDS.keyBy(jsonObj ->
                jsonObj.getJSONObject("common").getString("mid"));
        Pattern<JSONObject, JSONObject> pattern = Pattern.<JSONObject>begin("begin").where(
                new SimpleCondition<JSONObject>() {
                    @Override
                    public boolean filter(JSONObject jsonObject) throws Exception {
                        String lastPageId =
                                jsonObject.getJSONObject("page").getString("last_page_id");
                        return lastPageId == null || lastPageId.length() <= 0;
                    }
                }).times(2) //默认的使用宽松近邻
                .consecutive() //指定使用严格近邻
                .within(Time.seconds(10));

//6.将模式序列作用在流上
        PatternStream<JSONObject> patternStream = CEP.pattern(keyedStream, pattern);

        //7.提取事件和超时事件
        OutputTag<JSONObject> outputTag = new OutputTag<JSONObject>("TimeOut") {
        };

        SingleOutputStreamOperator<JSONObject> selectDS = patternStream.select(outputTag,
                new PatternTimeoutFunction<JSONObject, JSONObject>() {
                    @Override
                    public JSONObject timeout(Map<String, List<JSONObject>> map, long l)
                            throws Exception {
                        //提取事件
                        List<JSONObject> begin = map.get("begin");
                        return begin.get(0);
                    }
                },
                new PatternSelectFunction<JSONObject, JSONObject>() {
                    @Override
                    public JSONObject select(Map<String, List<JSONObject>> map) throws
                            Exception {
                        //提取事件
                        List<JSONObject> begin = map.get("begin");
                        return begin.get(0);
                    }

                });

//8.将数据写入 Kafka
        DataStream<JSONObject> userJumpDetailDS = selectDS.getSideOutput(outputTag);
        DataStream<JSONObject> result = selectDS.union(userJumpDetailDS);
        result.print(">>>>>>>>>>>>>");
        result.map(JSONAware::toJSONString).addSink(MyKafkaUtil.getKafkaSink(sinkTopic));
        //9.执行任务
        env.execute();
    }

    public static class UniqueVisitApp {
        public static void main(String[] args) throws Exception {
            //1.获取执行环境
            StreamExecutionEnvironment env =
                    StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(1);
            //1.1 设置状态后端
            //env.setStateBackend(new
            // FsStateBackend("hdfs://hadoop102:8020/gmall/dwd_log/ck"));
            //1.2 开启 CK
            //env.enableCheckpointing(10000L, CheckpointingMode.EXACTLY_ONCE);
            //env.getCheckpointConfig().setCheckpointTimeout(60000L);
            //2.读取 Kafka dwd_page_log 主题数据创建流
            String groupId = "unique_visit_app";
            String sourceTopic = "dwd_page_log";
            String sinkTopic = "dwm_unique_visit";
            FlinkKafkaConsumer<String> kafkaSource = MyKafkaUtil.getKafkaSource(sourceTopic,
                    groupId);
            DataStreamSource<String> kafkaDS = env.addSource(kafkaSource);
            //3.将每行数据转换为 JSON 对象
            SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.process(
                    new ProcessFunction<String, JSONObject>() {
                        @Override
                        public void processElement(String s, Context context, Collector<JSONObject>
                                collector) throws Exception {
                            try {
                                JSONObject jsonObject = JSON.parseObject(s);
                                collector.collect(jsonObject);
                            } catch (Exception e) {
                                context.output(new OutputTag<String>("dirty") {
                                }, s);
                            }
                        }
                    });

           // jsonObjDS.print();
            //4.按照 mid 分组
            KeyedStream<JSONObject, String> keyedStream = jsonObjDS.keyBy(jsonObj ->
                    jsonObj.getJSONObject("common").getString("mid"));
    //5.过滤掉不是今天第一次访问的数据
            SingleOutputStreamOperator<JSONObject> filterDS = keyedStream.filter(
                    new RichFilterFunction<JSONObject>() {
                        //声明状态
                        private ValueState<String> firstVisitState;
                        private SimpleDateFormat simpleDateFormat;

                        @Override
                        public void open(Configuration parameters) throws Exception {
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            ValueStateDescriptor<String> stringValueStateDescriptor = new
                                    ValueStateDescriptor<>("visit-state", String.class);
                            //创建状态 TTL 配置项
                            StateTtlConfig stateTtlConfig = StateTtlConfig.newBuilder(org.apache.flink.api.common.time.Time.days(1))
                                    .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                                    .build();
                            stringValueStateDescriptor.enableTimeToLive(stateTtlConfig);
                            firstVisitState = getRuntimeContext().getState(stringValueStateDescriptor);
                        }

                        @Override
                        public boolean filter(JSONObject value) throws Exception {
                            //取出上一次访问页面
                            String lastPageId = value.getJSONObject("page").getString("last_page_id");
                            //判断是否存在上一个页面
                            if (lastPageId == null || lastPageId.length() <= 0) {
                                //取出状态数据
                                String firstVisitDate = firstVisitState.value();
                                //取出数据时间
                                Long ts = value.getLong("ts");
                                String curDate = simpleDateFormat.format(ts);
                                if (firstVisitDate == null || !firstVisitDate.equals(curDate)) {
                                    firstVisitState.update(curDate);
                                    return true;
                                } else {
                                    return false;
                                }
                            } else {

                                return false;
                            }
                        }
                    });

            filterDS.print(">>>>>>>>>");
            filterDS.map(JSON::toString).addSink(MyKafkaUtil.getKafkaSink(sinkTopic));

            //7.启动任务
            env.execute();
        }
    }
}
