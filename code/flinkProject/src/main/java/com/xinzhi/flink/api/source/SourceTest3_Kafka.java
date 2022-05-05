package com.xinzhi.flink.api.source;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;



public class SourceTest3_Kafka {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        Properties properties=new Properties();
        properties.setProperty("bootstrap.servers","VM-4-15-centos:9092,VM-4-15-centos:9093,VM-4-15-centos:9094");
        properties.setProperty("group.id","test-consumer-group");
        properties.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("auto.offset.reset", "earliest");
        DataStream<String> stream = env
                .addSource(
                new FlinkKafkaConsumer<String>("test",
                new SimpleStringSchema(),
                properties)
                );

        stream.print();
        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
