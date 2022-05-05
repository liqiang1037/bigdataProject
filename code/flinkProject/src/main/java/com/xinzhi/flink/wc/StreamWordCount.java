package com.xinzhi.flink.wc;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 *         streamEnv.setParallelism(32);
 * Caused by: java.util.concurrent.CompletionException: org.apache.flink.runtime.jobmanager.scheduler.NoResourceAvailableException: Slot request bulk is not fulfillable! Could not allocate
 * the required slot within slot request timeout
 *
 */
public class StreamWordCount {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment streamEnv=StreamExecutionEnvironment.getExecutionEnvironment();
        streamEnv.setParallelism(4);
        String wordPath="/test/flink/file/word.txt";
        DataStreamSource<String> inputStream= streamEnv.readTextFile(wordPath);
        DataStream<Tuple2<String, Integer>> result = inputStream
                .flatMap(new WordCount.MyFlatMapper())
                .keyBy(item -> item.f0)
                .sum(1);
        result.print();
        streamEnv.execute();


    }
}