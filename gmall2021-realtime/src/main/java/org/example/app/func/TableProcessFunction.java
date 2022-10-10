package org.example.app.func;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.example.bean.TableProcess;
import org.example.common.GmallConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableProcessFunction extends BroadcastProcessFunction<JSONObject, String, JSONObject> {

    private OutputTag<JSONObject> objectOutputTag;
    private MapStateDescriptor<String, TableProcess> mapStateDescriptor;
    private Connection connection;

    public TableProcessFunction(OutputTag<JSONObject> objectOutputTag, MapStateDescriptor<String, TableProcess> mapStateDescriptor) {
        this.objectOutputTag = objectOutputTag;
        this.mapStateDescriptor = mapStateDescriptor;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
       // System.out.println("12321312");
        Class.forName(GmallConfig.PHOENIX_DRIVER);
        connection = DriverManager.getConnection(GmallConfig.PHOENIX_SERVER);
    }

    //value:{"db":"","tn":"","before":{},"after":{},"type":""}
    @Override
    public void processBroadcastElement(String value, Context ctx, Collector<JSONObject> out) throws Exception {
//获取状态
        BroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
        //将配置信息流中的数据转换为 JSON 对象{"database":"","table":"","type","","data":{"":""}}
        JSONObject jsonObject = JSON.parseObject(value);
       // System.out.println("jsonObject==>"+jsonObject);
        //取出数据中的表名以及操作类型封装 key
        JSONObject data = jsonObject.getJSONObject("data");
        String table = data.getString("source_table");
        String type = data.getString("operate_type");

        String key = table + ":" + type;
        //取出 Value 数据封装为 TableProcess 对象
        TableProcess tableProcess = JSON.parseObject(data.toString(), TableProcess.class);
       // System.out.println("广播流  tableProcess.getSinkType()===>"+tableProcess.getSinkType());
        checkTable(tableProcess.getSinkTable(), tableProcess.getSinkColumns(), tableProcess.getSinkPk(), tableProcess.getSinkExtend());
        //System.out.println("广播流 Key:" + key + "," + tableProcess);
        //广播出去
        broadcastState.put(key, tableProcess);
    }


    //建表语句 : create table if not exists db.tn(id varchar primary key,tm_name varchar) xxx;
    private void checkTable(String sinkTable, String sinkColumns, String sinkPk, String sinkExtend) {

        PreparedStatement preparedStatement = null;

        try {
            if (sinkPk == null) {
                sinkPk = "id";
            }
            if (sinkExtend == null) {
                sinkExtend = "";
            }

            StringBuffer createTableSQL = new StringBuffer("create table if not exists ")
                    .append(GmallConfig.HBASE_SCHEMA)
                    .append(".")
                    .append(sinkTable)
                    .append("(");

            String[] fields = sinkColumns.split(",");

            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];

                //判断是否为主键
                if (sinkPk.equals(field)) {
                    createTableSQL.append(field).append(" varchar primary key ");
                } else {
                    createTableSQL.append(field).append(" varchar ");
                }

                //判断是否为最后一个字段,如果不是,则添加","
                if (i < fields.length - 1) {
                    createTableSQL.append(",");
                }
            }

            createTableSQL.append(")").append(sinkExtend);

            //打印建表语句
          //  System.out.println("\"Phoenix表\" + sinkTable +"+createTableSQL);

            //预编译SQL
            preparedStatement = connection.prepareStatement(createTableSQL.toString());

            //执行
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Phoenix表" + sinkTable + "建表失败！");
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //value:{"db":"","tn":"","before":{},"after":{},"type":""}
    @Override
    public void processElement(JSONObject jsonObject, ReadOnlyContext readOnlyContext, Collector<JSONObject> collector) throws Exception {

        //获取状态
        ReadOnlyBroadcastState<String, TableProcess> broadcastState = readOnlyContext.getBroadcastState(mapStateDescriptor);
        //获取表名和操作类型
        String table = jsonObject.getString("table");
        String type = jsonObject.getString("operation");
        if("create".equals(type)){
            type="insert";
        }
        String key = table + ":" + type;

        //取出对应的配置信息数据
        TableProcess tableProcess = broadcastState.get(key);
        if (tableProcess != null) {
            //向数据中追加 sink_table 信息
            jsonObject.put("sink_table", tableProcess.getSinkTable());
            //根据配置信息中提供的字段做数据过滤
            filterColumn(jsonObject.getJSONObject("data"), tableProcess.getSinkColumns());
            //判断当前数据应该写往 HBASE 还是 Kafka
            //System.out.println("主流  tableProcess.getSinkType()===>"+tableProcess.getSinkType());
            if (TableProcess.SINK_TYPE_KAFKA.equals(tableProcess.getSinkType())) {
                //Kafka 数据,将数据输出到主流
                try {
                    collector.collect(jsonObject);
                }catch (Exception e){
                    System.out.println("出错的数据--》"+jsonObject);
                    e.printStackTrace();
                }
            } else if (TableProcess.SINK_TYPE_HBASE.equals(tableProcess.getSinkType())) {
                //HBase 数据,将数据输出到侧输出流
                readOnlyContext.output(objectOutputTag, jsonObject);
            }
        } else {
            System.out.println("No Key " + key + " In Mysql!");
        }
    }

    /**
     * @param data        {"id":"11","tm_name":"atguigu","logo_url":"aaa"}
     * @param sinkColumns id,tm_name
     *                    {"id":"11","tm_name":"atguigu"}
     */
    private void filterColumn(JSONObject data, String sinkColumns) {
        //保留的数据字段
        String[] fields = sinkColumns.split(",");
        List<String> fieldList = Arrays.asList(fields);
        Set<Map.Entry<String, Object>> entries = data.entrySet();

        entries.removeIf(next -> !fieldList.contains(next.getKey()));
    }
}