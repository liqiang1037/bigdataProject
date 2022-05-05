package com.xinzhi.flink.api.source;

public class Test {
    public static void main(String[] args) {
        String str="sensor_1,1547718199,35.8";
    int   length= str.split(",").length;
        System.out.println(length);
    }
}
