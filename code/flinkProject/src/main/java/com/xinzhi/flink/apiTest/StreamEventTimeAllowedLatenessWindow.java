package com.xinzhi.flink.apiTest;

import com.xinzhi.flink.utils.Order;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * @author liu a fu
 * @version 1.0
 * @date 2021/3/9 0009
 * @DESC 滚动事件时间窗口（Tumbling EventTime Window）统计：每隔5秒，计算5秒内，每个用户的订单金额
 *               TODO：解决延迟数据   通过 Allowed Lateness机制和侧边流处理延迟数据。
 */
public class StreamEventTimeAllowedLatenessWindow {
    public static void main(String[] args) throws Exception {
        // 1. 执行环境-env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1) ;

        // 2. 数据源-source inputStream

        DataStreamSource<String> inputStream = env.socketTextStream("localhost", 7777);;
        DataStream<Order> orderStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new Order(fields[0], new Integer(fields[1]), new Long(fields[2]));
        });

        // TODO: step2. 提取事件时间字段值，转换为Long类型，并且设置最大允许的延迟时间或乱序时间
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss") ;
        SingleOutputStreamOperator<Order> timeStream = orderStream.assignTimestampsAndWatermarks(
                new AssignerWithPeriodicWatermarks<Order>() {
                    // Watermark延迟时间（允许最大数据乱序时间）, 此处设置最大延迟2秒
                    Long maxOutOfOrderness = 2000L;
                    // 当前最大事件时间
                    Long currentMaxTimestamp = Long.MIN_VALUE + 2000L;
                    // 最新的水位时间
                    Long lastEmittedWatermark = Long.MIN_VALUE;

                    @Nullable
                    @Override
                    public Watermark getCurrentWatermark() {
                        // 依据当前数据中事件时间，计算出水位Watermark值
                        long potentialWatermark = currentMaxTimestamp - maxOutOfOrderness;
                        // 比较当前数据计算Watermark值与上次数据Watermark值大小，设置Watermark
                        if(potentialWatermark >= lastEmittedWatermark){
                            lastEmittedWatermark = potentialWatermark ;
                        }
                        return new Watermark(lastEmittedWatermark);
                    }

                    @Override
                    public long extractTimestamp(Order order, long previousElementTimestamp) {
                        Long eventTime = order.getOrderTime();

                        // 比较当前数据事件时间与最大事件时间大小
                        if (eventTime > currentMaxTimestamp) {
                            currentMaxTimestamp = eventTime;
                        }

                        // 打印当前数据Watermark值
                        System.out.println(
                                "userId: " + order.getUserId()
                                        + ", money: " + order.getMoney()
                                        + ", eventTime: " + format.format(eventTime)
                                        + ", watermark: " + format.format(getCurrentWatermark().getTimestamp())
                        );
                        return eventTime;
                    }
                }
        );
        // TODO:第二 创建输出标签Tag  侧边流要创建
        final OutputTag<Order> lateOutputTag= new OutputTag<Order>("late-data") {};

        // 3. 数据转换-transformation: 每隔5秒，计算5秒内，每个用户的订单金额
        SingleOutputStreamOperator<String> sumDataStream = timeStream

                .keyBy("userId")
                // TODO： 设置事件时间窗口，大小为5秒
                //设置窗口大小
                .timeWindow(Time.seconds(5))
                // TODO: 第一、设置允许延迟数据最大时间
                .allowedLateness(Time.seconds(5))
                //TODO: 第三设置侧边流
                .sideOutputLateData(lateOutputTag)
                // 窗口函数聚合
                .apply(
                        new WindowFunction<Order, String, Tuple, TimeWindow>() {
                            @Override
                            public void apply(Tuple tuple,
                                              TimeWindow window,
                                              Iterable<Order> input,
                                              Collector<String> out) throws Exception {
                                // 获取窗口StartTime和EndTime
                                String startTime = format.format(window.getStart());
                                String endTime = format.format(window.getEnd());
                                // 获取组合所有订单时间
                                ArrayList<String> list = new ArrayList<>();
                                Integer orderSum = 0;

                                for (Order order : input) {
                                    list.add(format.format(order.getOrderTime()));
                                    orderSum += order.getMoney();
                                }
                                String output = "窗口>>> userId: " + tuple.toString()
                                        + " -> [" + startTime + " ~ " + endTime
                                        + "],  sum： " + orderSum + ", Orders: " + list;

                                out.collect(output);
                            }
                        });
        // 4. 数据终端-sink
        sumDataStream.printToErr();

        // TODO: 第四、获取侧边流数据
        DataStream<Order> lateDataStream = sumDataStream.getSideOutput(lateOutputTag);
        lateDataStream.print("late>>") ;

        // 5. 触发执行-execute
        env.execute(StreamEventTimeAllowedLatenessWindow.class.getSimpleName());
    }
}

