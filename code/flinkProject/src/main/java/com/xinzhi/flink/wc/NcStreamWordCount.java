package com.xinzhi.flink.wc;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class NcStreamWordCount {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment streaEnv=StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> ncStream = streaEnv.socketTextStream("localhost", 7777)
                ;
        DataStream<Tuple2<String, Integer>> result = ncStream
                .flatMap(new WordCount.MyFlatMapper())
                .keyBy(item -> item.f0)
                .sum(1);
        result.print();
        streaEnv.execute();



    }
}
