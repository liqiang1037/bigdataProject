package com.xinzhi.flink.wc;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class StreamWordCount {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment streamEnv=StreamExecutionEnvironment.getExecutionEnvironment();
        streamEnv.setMaxParallelism(32);
        String wordPath=args[0];
        DataStreamSource<String> inputStream= streamEnv.readTextFile(wordPath);
        DataStream<Tuple2<String, Integer>> result = inputStream.flatMap(new WordCount.MyFlatMapper()).keyBy(item -> item.f0).sum(1);
        result.print();
        streamEnv.execute();


    }
}
