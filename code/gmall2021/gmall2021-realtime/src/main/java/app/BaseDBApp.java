package app;

import bean.TableProcess;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource;
import com.alibaba.ververica.cdc.debezium.DebeziumSourceFunction;
import common.TableProcessFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import common.CustomDeserializationSchema;
import org.apache.flink.util.OutputTag;
import utils.MyKafkaUtil;

public class BaseDBApp {
    public static void main(String[] args) throws Exception {
        //1.获取执行环境
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        //1.1 设置状态后端
//env.setStateBackend(new
        // FsStateBackend("hdfs://hadoop102:8020/gmall/dwd_log/ck"));
//1.2 开启 CK
//env.enableCheckpointing(10000L, CheckpointingMode.EXACTLY_ONCE);
//env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.读取 Kafka 数据
        String topic = "ods_base_db";
        String groupId = "ods_db_group";
        FlinkKafkaConsumer<String> kafkaSource = MyKafkaUtil.getKafkaSource(topic,
                groupId);
        DataStreamSource<String> kafkaDS = env.addSource(kafkaSource);
        //3.将每行数据转换为 JSON 对象
        SingleOutputStreamOperator<JSONObject> jsonObjDS =
                kafkaDS.map(JSON::parseObject);
        //4.过滤
        SingleOutputStreamOperator<JSONObject> filterDS = jsonObjDS.filter(
                new FilterFunction<JSONObject>() {
                    @Override
                    public boolean filter(JSONObject value) throws Exception {
                        //获取 data 字段
                        String data = value.getString("data");
                        return data != null && data.length() > 0;
                    }
                });
        //打印测试
        // filterDS.print();
        //5.创建 MySQL CDC Source
        DebeziumSourceFunction<String> sourceFunction = MySQLSource.<String>builder()
                .hostname("hadoop102")
                .port(3306)
                .username("root")
                .password("000000")
                .databaseList("gmall2021-realtime")
                .tableList("gmall2021-realtime.table_process")
                .deserializer(new CustomDeserializationSchema()
                )
                .build();
        //6.读取 MySQL 数据
        DataStreamSource<String> tableProcessDS = env.addSource(sourceFunction);
        //7.将配置信息流作为广播流
        MapStateDescriptor<String, TableProcess> mapStateDescriptor = new
                MapStateDescriptor<>("table-process-state", String.class, TableProcess.class);
        BroadcastStream<String> broadcastStream = tableProcessDS.broadcast(mapStateDescriptor);
        //8.将主流和广播流进行链接
        BroadcastConnectedStream<JSONObject, String> connectedStream =
                filterDS.connect(broadcastStream);
        OutputTag<JSONObject> hbaseTag = new
                OutputTag<JSONObject>(TableProcess.SINK_TYPE_HBASE) {
                };
        SingleOutputStreamOperator<JSONObject> kafkaJsonDS = connectedStream.process(new
                TableProcessFunction(hbaseTag,mapStateDescriptor));
        DataStream<JSONObject> hbaseJsonDS = kafkaJsonDS.getSideOutput(hbaseTag);
        //7.执行任务
        env.execute();
    }
}
