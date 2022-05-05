package com.xinzhi.puke;

import java.util.ArrayList;


import java.util.List;


public class PlayDemo {
    static Cards cards;
    static List<Card> cardList;
    static List<Player> p = new ArrayList();
    static int k = 0;


    //创建玩家
    public List<Player> setPlayer() {
        Player p1 = new Player(1, "李强爸爸");
        Player p2 = new Player(1, "鹏云孙子");
        Player p3 = new Player(1, "丑丑大儿");
        p.add(p1);
        p.add(p2);
        p.add(p3);
        return p;

    }

    public int play(List<Card> cardList) {

        //(5)扑克牌比大小游戏开始啦~
        //设定每人分别拿两张(or多张)
        //玩家轮流拿牌
        int count = 0;
        for (int j = 0; j < p.size(); j++) {
            Card card = cardList.get(count);
            p.get(j).setHandCards(card);
            cardList.remove(card);
            count++;
        }
        Card card = cardList.remove(count);
        for (int j = 0; j < p.size(); j++) {
            p.get(j).setHandCards(card);
        }
        count++;
        for (int i = 0; i < p.size(); i++) {
            System.out.println("玩家" + p.get(i).getName() + "的手牌为:");
            for (int j = 0 + 2 * k; j < 2 + 2 * k; j++) {
                Card cur = (Card) p.get(i).getHandCards().get(j);
                System.out.println(cur.getColor() + cur.getNumber() + " ");
            }

        }
        k++;

        System.out.println("剩余牌的个数" + cardList.size());
        return count;


    }

    public static void main(String[] args) {
        //测试简易扑克牌程序
        PlayDemo game = new PlayDemo();
        //(1)创建一副牌
        cards = new Cards();
        //(2)展示新的扑克牌
        cards.showCards();
        //(3)洗牌
        cards.shufCards();
        //获取牌
        cardList = cards.getList();
        //创建玩家
        game.setPlayer();
        game.play(cardList);


    }
}