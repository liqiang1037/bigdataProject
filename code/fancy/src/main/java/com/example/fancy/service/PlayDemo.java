package com.example.fancy.service;

import com.example.fancy.bean.Card;
import com.example.fancy.bean.Player;

import java.util.ArrayList;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlayDemo {

    static List<Player> p = new ArrayList();


    //创建玩家
    public List<Player> setPlayer() {
        Player p1 = new Player("1");
        Player p2 = new Player("2");
        Player p3 = new Player("3");
        Player p4 = new Player("4");

        p.add(p1);
        p.add(p2);
        p.add(p3);
        p.add(p4);

        return p;

    }

    public List<String> getPlayer() {
        List<String> list = new ArrayList();
        for (Player pName : p) {
            list.add(pName.getName());
        }
        return list;
    }

    public Map<String,String> play(List<Card> cardList,  String flag) {
        Map<String,String> map=new HashMap<>();
        if (cardList.size() > p.size()) {
            if (flag.equals("1")) {

                //(5)扑克牌比大小游戏开始啦~
                //设定每人分别拿两张(or多张)
                //玩家轮流拿牌

                for (int j = p.size()-1; j >=0; j--) {
                    Card card = cardList.get(j);
                    p.get(j).setHandCards(card);
                    cardList.remove(j);
                    map.put(p.get(j).getName(), "/image/"+card.getColor()+ card.getNumber()+".png");

                }
                Card card = cardList.remove(0);
                for (int j = 0; j < p.size(); j++) {
                    p.get(j).setHandCards(card);
                    map.put("common", "/image/"+card.getColor()+ card.getNumber()+".png");
                }

            }
        }

        return map;
    }

}