package com.example.fancy.bean;

import lombok.Data;

@Data
public class Puke {
    //来源表
    public String name;
    //操作类型 insert,update,delete
    public String flag;
    //输出类型 hbase kafka
    public String poker;

    public Puke(String name, String flag, String poker, String addr) {
        this.name = name;
        this.flag = flag;
        this.poker = poker;
        this.addr = addr;
    }

    //牌的地址
    public String addr;

    public Puke(String name, String flag, String poker) {
        this.name = name;
        this.flag = flag;
        this.poker = poker;
    }
}
