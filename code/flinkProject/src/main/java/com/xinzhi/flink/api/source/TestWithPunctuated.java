package com.xinzhi.flink.api.source;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;

//在assignTimestampsAndWatermarks中，通过AssignerWithPunctuatedWatermarks抽取Timestamp和生成非周期性水位线示例
public class TestWithPunctuated{
    public static void main(String[] args) throws  Exception{
        //创建流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置EventTime语义
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //并行度1
        env.setParallelism(1);
        //演示数据
        DataStreamSource<ClickEvent> mySource = env.fromElements(
                new ClickEvent("user1", 1L, 1),
                new ClickEvent("user1", 2L, 2),
                new ClickEvent("user1", 3L, 3),
                new ClickEvent("user1", 4L, 4),
                new ClickEvent("user1", 5L, 5),
                new ClickEvent("user1", 6L, 6),
                new ClickEvent("user1", 7L, 7),
                new ClickEvent("user1", 8L, 8)
        );
        //AssignerWithPunctuatedWatermarks可定制规则来生成水位线
        SingleOutputStreamOperator<ClickEvent> streamTS = mySource.assignTimestampsAndWatermarks(
                new AssignerWithPunctuatedWatermarks<ClickEvent>() {
                    private long maxTimestamp = 0L;
                    //延迟
                    private long delay = 0L;
                    @Override
                    //自定义Timestamp提取规则
                    public long extractTimestamp(ClickEvent event, long l) {
                        try {
                            //放慢处理速度，否则可能只会生成一条水位线
                            Thread.sleep(100L);
                        } catch (Exception ex) {
                        }
                        //比较当前事件时间和最大时间戳maxTimestamp（并更新）
                        maxTimestamp = Math.max(event.getDatatime(), maxTimestamp);
                        System.out.println("时间："+event.getDatatime());
                        //提取时间戳
                        return event.getDatatime();
                    }
                    @Nullable
                    @Override
                    //决定如何生成watermark
                    public Watermark checkAndGetNextWatermark(ClickEvent event,long extractedTimestamp) {
                        //Value为3的倍数时，生成watermark
                        if (event.getId() % 3 == 0) {
                            System.out.println("水位线：" + (maxTimestamp - delay));
                            return new Watermark(maxTimestamp - delay);
                        } else {
                            //其他情况不返回水位线
                            return null;
                        }
                    }
                });
        //结果打印
        streamTS.print();
        env.execute();
    }
}
