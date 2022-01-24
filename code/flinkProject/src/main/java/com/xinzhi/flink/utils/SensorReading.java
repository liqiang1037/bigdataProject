package com.xinzhi.flink.utils;

public class SensorReading {
private  String id;
private Double temperature;
private Long timeStamp;

    public SensorReading(String id, Double temperature, Long timeStamp) {
        this.id = id;
        this.temperature = temperature;
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "SensorReading{" +
                "id='" + id + '\'' +
                ", temperature=" + temperature +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
