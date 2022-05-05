package com.example.fancy.config;

public class PassWord {
    public static void main(String[] args) {

        while (1 == 1) {
            Integer randNum = (int) (Math.random() * (999999) + 1);//产生(0,999999]之间的随机数
            String workPassWord = String.format("%06d", randNum);//进行六位数补全
            System.out.println(workPassWord);
            if ("999999".equals(workPassWord)) {//小小的测试一下
                break;
            }
        }
    }
}
