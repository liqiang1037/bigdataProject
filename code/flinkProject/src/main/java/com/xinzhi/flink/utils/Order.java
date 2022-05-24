package com.xinzhi.flink.utils;

public class Order {
    public String userId;

    public Order(String userId, int money, long orderTime) {
        this.userId = userId;
        this.money = money;
        OrderTime = orderTime;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public  int money;

    public long getOrderTime() {
        return OrderTime;
    }

    public void setOrderTime(long orderTime) {
        OrderTime = orderTime;
    }

    public long OrderTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }



}
