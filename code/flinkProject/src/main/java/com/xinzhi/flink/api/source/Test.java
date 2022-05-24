package com.xinzhi.flink.api.source;

public class Test {
    public static void main(String[] args) {
        long value= 1547718100;
        long size = 20;
        long start = value - (value - 0 + size) % size;
        System.out.println(start);
    }
}
