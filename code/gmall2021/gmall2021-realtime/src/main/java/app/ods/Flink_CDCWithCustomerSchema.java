package app.ods;


import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource;
import com.alibaba.ververica.cdc.connectors.mysql.table.StartupOptions;

import com.alibaba.ververica.cdc.debezium.DebeziumSourceFunction;


import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import common.CustomDeserializationSchema;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
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

    public static class FlinkCDC {
        //  获取执行环境
        public static void main(String[] args) throws Exception {
            StreamExecutionEnvironment env =
                    StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(1);
            env.enableCheckpointing(5000L);
            env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
            env.getCheckpointConfig().enableExternalizedCheckpoints(
                    CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
            //2.4 指定从 CK 自动重启策略
            env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 2000L));
            //2.5 设置状态后端
            env.setStateBackend(new FsStateBackend("hdfs://VM-4-7-centos:8020/flinkCDC"));
            DebeziumSourceFunction<String> mysqlSource = MySQLSource.<String>builder()
                    .hostname("VM-4-7-centos")
                    .port(3306)
                    .username("root")
                    .password("3edcCFT^")
                    .databaseList("gmall2021")
                    .tableList("gmall2021.test")
                    .startupOptions(StartupOptions.initial())
                    .deserializer(new CustomDeserializationSchema())
                    .build();
            //4.使用 CDC Source 从 MySQL 读取数据
            DataStreamSource<String> mysqlDS = env.addSource(mysqlSource);
            //5.打印数据
            mysqlDS.print();
            //6.执行任务
            env.execute();




        }
    }

    public static class FlinkSQL_CDC {
        public static void main(String[] args) throws Exception {
            //1.创建执行环境
            StreamExecutionEnvironment env =
                    StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(1);
            StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
            //2.创建 Flink-MySQL-CDC 的 Source
            tableEnv.executeSql("CREATE TABLE mysql_binlog (id INT NOT NULL," +
                    " name STRING, age INT) WITH ('connector' = 'mysql-cdc', " +
                    "'hostname' = 'vm-4-7-centos', 'port' = '3306', 'username' = 'root'," +
                    " 'password' = '3edcCFT^', 'database-name' = 'gmall2021'," +
                    " 'table-name' = 'test')");

            tableEnv.executeSql("select * from mysql_binlog").print();
            env.execute();
        }
    }
}