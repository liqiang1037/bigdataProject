package app;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class FlinkSQL_CDC {
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
