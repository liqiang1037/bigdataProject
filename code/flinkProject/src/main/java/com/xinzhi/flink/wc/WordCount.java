package com.xinzhi.flink.wc;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.util.Collector;
import scala.Tuple2;

public class WordCount {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(32);
        String inputPath="/test/flink";
        DataSource<String> textFileStream = env.readTextFile(inputPath);
        DataSet<Tuple2<String, Integer>> result = textFileStream.flatMap(new SplitFlatMap())
                .groupBy(0)
                .sum(1);

            result.print();



    }
}

class SplitFlatMap  implements FlatMapFunction{
    @Override
    public void flatMap(Object o, Collector collector) throws Exception {

        String[] str = o.toString().split(" ");
        for(String s:str){
            collector.collect(new Tuple2<String,Integer>(s,1));
        }

    }
}
