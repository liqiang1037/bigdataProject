package utils;

import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

public class MyKafkaUtil {
    private static String KAFKA_SERVER =
            "localhost:9092,localhost:9093,localhost:9094";
    private static String KAFKA_SERVER_LOCALHOST =
            "vm-4-7-centos:9092,vm-4-7-centos:9093,vm-4-7-centos:9094";
    private static Properties properties = new Properties();
    static {
        properties.setProperty("bootstrap.servers", KAFKA_SERVER);
    }
    public static FlinkKafkaProducer<String> getKafkaSink(String topic) {
        return new FlinkKafkaProducer<String>(topic, new SimpleStringSchema(), properties);
    }

    public static FlinkKafkaConsumer<String> getKafkaSource(String topic, String groupId) {
        //给配置信息对象添加配置项
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty("auto.offset.reset", "latest");

        //获取 KafkaSource
        return new FlinkKafkaConsumer<String>(topic, new SimpleStringSchema(), properties);
    }
}