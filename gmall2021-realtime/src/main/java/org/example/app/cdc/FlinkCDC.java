package org.example.app.cdc;

import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource;
import com.alibaba.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.alibaba.ververica.cdc.debezium.DebeziumSourceFunction;
import com.alibaba.ververica.cdc.debezium.StringDebeziumDeserializationSchema;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.example.utils.MyKafkaUtil;

public class FlinkCDC {
    public static void main(String[] args)  throws Exception{
        //1.创建执行环境
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        //2.Flink-CDC 将读取 binlog 的位置信息以状态的方式保存在 CK,如果想要做到断点续传,需要从 Checkpoint 或者 Savepoint 启动程序
        //2.1 开启 Checkpoint,每隔 5 秒钟做一次 CK
        env.enableCheckpointing(5000L);
        //2.2 指定 CK 的一致性语义
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //2.3 设置任务关闭的时候保留最后一次 CK 数据
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //2.4 指定从 CK 自动重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 2000L));
        //2.5 设置状态后端
        env.setStateBackend(new FsStateBackend("hdfs://hadoop100:8020/flinkCDC"));
        DebeziumSourceFunction<String> mysqlSource = MySQLSource.<String>builder()
                .hostname("hadoop102")
                .port(3306)
                .username("root")
                .password("3edcCFT^")
                .databaseList("gmall2021")
                .tableList("gmall2021.actor") //可选配置项,如果不指定该参数,则会 读取上一个配置下的所有表的数据，注意：指定的时候需要使用"db.table"的方式
                .startupOptions(StartupOptions.earliest())
                .deserializer(new StringDebeziumDeserializationSchema())
                .build();
        //4.使用 CDC Source 从 MySQL 读取数据
        DataStreamSource<String> mysqlDS = env.addSource(mysqlSource);
        //5.打印数据
        mysqlDS.addSink(MyKafkaUtil.getKafkaSink("ods_base_db1"));

        //6.执行任务
        env.execute();
    } }