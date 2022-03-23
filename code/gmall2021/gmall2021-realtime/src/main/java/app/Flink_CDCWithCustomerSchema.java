package app;


import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource;
import com.alibaba.ververica.cdc.connectors.mysql.table.StartupOptions;

import com.alibaba.ververica.cdc.debezium.DebeziumSourceFunction;


import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import common.CustomDeserializationSchema;
import utils.MyKafkaUtil;

public class Flink_CDCWithCustomerSchema {
    public static void main(String[] args) throws Exception {
        //1.创建执行环境
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        DebeziumSourceFunction<String> mysqlSource = MySQLSource.<String>builder()
                .hostname("VM-4-7-centos")
                .port(3306)
                .username("root")
                .password("3edcCFT^")
                .databaseList("gmall2021")
                .startupOptions(StartupOptions.latest())
                .deserializer(new CustomDeserializationSchema())
                .build();
        //3.使用 CDC Source 从 MySQL 读取数据
        DataStreamSource<String> mysqlDS = env.addSource(mysqlSource);
        //4.打印数据
       // mysqlDS.print();
           mysqlDS.addSink(MyKafkaUtil.getKafkaSink("ods_base_db"));
        //5.执行任务
        env.execute();
    }
}