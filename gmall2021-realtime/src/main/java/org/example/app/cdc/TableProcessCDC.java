package org.example.app.cdc;

import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource;
import com.alibaba.ververica.cdc.debezium.DebeziumSourceFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.example.app.func.CustomerDeserialization;
import org.example.bean.TableProcess;

public class TableProcessCDC {
    public static void main(String[] args) throws Exception{
        //1.获取执行环境
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        //5.创建 MySQL CDC Source
        DebeziumSourceFunction<String> sourceFunction = MySQLSource.<String>builder()
                .hostname("hadoop102")
                .port(3306)
                .username("root")
                .password("3edcCFT^")
                .databaseList("gmall2021_realtime")
                .tableList("gmall2021_realtime.table_process")
                .deserializer(new CustomerDeserialization())
                .build();
//6.读取 MySQL 数据
        DataStreamSource<String> tableProcessDS = env.addSource(sourceFunction);
//7.将配置信息流作为广播流
        MapStateDescriptor<String, TableProcess> mapStateDescriptor = new
                MapStateDescriptor<>("table-process-state", String.class, TableProcess.class);
        BroadcastStream<String> broadcastStream = tableProcessDS.broadcast(mapStateDescriptor);
        env.execute();

    }
}
