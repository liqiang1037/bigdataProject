package com.xinzhi.flink.api.source;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author : Ashiamd email: ashiamd@foxmail.com
 * @date : 2021/2/1 7:14 PM
 */
public class WindowTest1_TimeWindow {
    public static void main(String[] args) throws Exception {

        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 并行度设置1，方便看结果
        env.setParallelism(1);

        //        // 从文件读取数据
        //        DataStream<String> dataStream = env.readTextFile("/tmp/Flink_Tutorial/src/main/resources/sensor.txt");

        // 从socket文本流获取数据
        DataStream<String> inputStream = env.socketTextStream("flink", 7777);

        // 转换成SensorReading类型
        DataStream<SensorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            System.out.println(dtf.format(now));
            if(fields.length==3){
                return new SensorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
            }else{
                return new SensorReading("sensor_0", 1547718199L, 37.3);
            }

        });

        // 开窗测试

        // 1. 增量聚合函数 (这里简单统计每个key组里传感器信息的总数)
        DataStream<Double> resultStream = dataStream.keyBy("id")
                //                .countWindow(10, 2);
                //                .window(EventTimeSessionWindows.withGap(Time.minutes(1)));
                //                .window(TumblingProcessingTimeWindows.of(Time.seconds(15)))
                //                .timeWindow(Time.seconds(15)) // 已经不建议使用@Deprecated
                .window(TumblingProcessingTimeWindows.of(Time.seconds(15)))
                .aggregate(new AggregateFunction<SensorReading, Double, Double>() {


                    @Override
                    public Double createAccumulator() {
                        return 0.0;
                    }

                    @Override
                    public Double add(SensorReading value, Double accumulator) {
                        return accumulator+value.getTemperature();
                    }

                    @Override
                    public Double getResult(Double accumulator) {
                        return accumulator;
                    }

                    @Override
                    public Double merge(Double a, Double b) {
                        return a+b;
                    }
                });

        resultStream.print("result");

        env.execute();
    }


}