package com.xinzhi.flink.apiTest;

import com.xinzhi.flink.utils.SensorReading;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;

public class Source_Collection_Test1 {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment streamEnv=StreamExecutionEnvironment.getExecutionEnvironment();
        streamEnv.setParallelism(4);
        DataStreamSource<SensorReading> source= streamEnv.fromCollection(Arrays.asList(
                new SensorReading("1", 36.1, 1643013898L),
                new SensorReading("2", 36.2, 1643013899L),
                new SensorReading("3", 36.3, 1643013900L)

        ));
        source.print("sensor");
        streamEnv.execute("1");
    }
}
