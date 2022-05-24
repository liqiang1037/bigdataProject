package com.xinzhi.flink.api.source;

public class ClickEvent {
    public  long datatime;

    public long getDatatime() {
        return datatime;
    }

    public void setDatatime(long datatime) {
        this.datatime = datatime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public  String name;
    public  int id;
    public ClickEvent(String user1, long l, int i) {
    }
}
