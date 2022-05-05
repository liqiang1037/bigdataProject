package com.xinzhi.flink.apiTest;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Source_File_Test {
    public static void main(String[] args) {
        StreamExecutionEnvironment streamEnv=StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> a = streamEnv.readTextFile("/test/flink/file/word.txt");



    }
}
