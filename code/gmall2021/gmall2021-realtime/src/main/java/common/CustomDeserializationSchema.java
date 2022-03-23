package common;


import com.alibaba.fastjson.JSONObject;

import com.alibaba.ververica.cdc.debezium.DebeziumDeserializationSchema;

import io.debezium.data.Envelope;

import org.apache.flink.api.common.typeinfo.TypeInformation;

import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;


public class CustomDeserializationSchema implements DebeziumDeserializationSchema {
    @Override
    public void deserialize(SourceRecord sourceRecord, Collector collector) throws Exception {
        //获取主题信息,包含着数据库和表名
        String topic = sourceRecord.topic();
        String[] arr = topic.split("\\.");
        String db = arr[1];
        String tableName = arr[2];
        //获取操作类型 READ DELETE UPDATE CREATE
        Envelope.Operation operation =
                Envelope.operationFor(sourceRecord);
        //获取值信息并转换为 Struct 类型
        Struct value = (Struct) sourceRecord.value();

        Struct after = value.getStruct("after");
        JSONObject data = new JSONObject();
        if (after != null) {
            //创建 JSON 对象用于存储数据信息

            for (Field field : after.schema().fields()) {
                Object o = after.get(field);
                data.put(field.name(), o);
            }
        }
        //创建 JSON 对象用于封装最终返回值数据信息
        JSONObject result = new JSONObject();
        if (operation.toString().toLowerCase().equals("create")) {
            result.put("type", "insert");
        } else {
            result.put("type", operation.toString().toLowerCase());
        }
        result.put("data", data);
        result.put("database", db);
        result.put("table", tableName);
        //发送数据至下游
        collector.collect(result.toJSONString());

    }

    @Override
    public TypeInformation getProducedType() {
        return TypeInformation.of(String.class);
    }
}
