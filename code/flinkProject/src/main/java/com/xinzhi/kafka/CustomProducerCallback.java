package com.xinzhi.kafka;

import org.apache.kafka.clients.producer.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class CustomProducerCallback {
    public static void main(String[] args) {
        Properties props = new Properties();
        //kafka 集群，broker-list
        props.put("bootstrap.servers", "VM-4-15-centos:9092,VM-4-15-centos:9093,VM-4-15-centos:9094");
        props.put("acks", "all");
        //重试次数
        props.put("retries", 1);
        //批次大小
        props.put("batch.size", 16384);
        //等待时间
        props.put("linger.ms", 1);
        //RecordAccumulator 缓冲区大小
        props.put("buffer.memory", 33554432);
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new
                KafkaProducer<>(props);
        int i = 0;

        while (i < 100000000) {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String currTime = formatter.format(calendar.getTime());

            producer.send(new ProducerRecord<String, String>("test",
                    Integer.toString(i), Integer.toString(i) + "==>" + currTime), new Callback() {
                //回调函数，该方法会在 Producer 收到 ack 时调用，为异步调用
                @Override
                public void onCompletion(RecordMetadata metadata,
                                         Exception exception) {
                    if (exception == null) {
                        System.out.println("success->" +
                                metadata.offset());
                    } else {
                        System.out.println("111111");
                        exception.printStackTrace();
                    }
                }
            });
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }

        producer.close();
    }
}