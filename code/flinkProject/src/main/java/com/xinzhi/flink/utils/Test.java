package com.xinzhi.flink.utils;

import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<SensorReading> sensorReadingList= Arrays.asList(
                new SensorReading("1", 36.1, 1643013898L),
                new SensorReading("2", 36.2, 1643013899L),
                new SensorReading("3", 36.3, 1643013900L)

        );
        for(SensorReading s:sensorReadingList){
            System.out.println(s);
        }

    }
}
