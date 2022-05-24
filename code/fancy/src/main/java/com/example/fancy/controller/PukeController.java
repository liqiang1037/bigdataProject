package com.example.fancy.controller;

import com.example.fancy.bean.*;
import com.example.fancy.service.PlayDemo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller

public class PukeController {
    //@DeleteMapping
    //@PutMapping
    //  @GetMapping
    //@RequestMapping(value = "/usr/login", method = RequestMethod.POST)
    static Cards cards;
    static List<Card> cardList;
    static PlayDemo game;
    static Map<String, String> imageMap;
    static Map<String, String> gameMap;
    static  int i=0;

    static {
        game = new PlayDemo();
        //(1)创建一副牌
        cards = new Cards();
        //(2)展示新的扑克牌
        cards.showCards();
        //(3)洗牌
        cards.shufCards();
        //获取牌
        cardList = cards.getList();

        cards.showCards();

        //创建玩家
        game.setPlayer();
    }

    @GetMapping(value = "/user/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Map<String, Object> map, Model model) {

        if (!StringUtils.isEmpty(username) && game.getPlayer().contains(username)) {
            if((password.equals("275172") && username.equals("1"))
                    ||(password.equals("443663") && username.equals("2"))
                    ||(password.equals("164098") && username.equals("3")))
            if (password.equals("275172") && username.equals("1")) {
                gameMap = game.play(cardList,"1");
                i++;
                imageMap = new HashMap<>();

            }
            if (gameMap != null) {
                System.out.println("第-->"+i+"把牌");

                //获取牌
                for (Map.Entry<String, String> entry : gameMap.entrySet()) {
                    String mapKey = entry.getKey();
                    System.out.println("玩家名称-->"+mapKey);
                    String mapValue = entry.getValue();
                    System.out.println("玩家牌-->"+mapValue);
                    if ("common".equals(mapKey)) {
                        model.addAttribute("common", mapValue);//普通字符串
                    } else {
                        imageMap.put(mapKey, mapValue);
                        model.addAttribute("map", imageMap);//储存Map
                    }
                }

                model.addAttribute("name", username);//普通字符串
                model.addAttribute("sum", game.getPlayer().size());//普通字符串
                model.addAttribute("count", cardList.size());//普通字符串
                return "index";
            } else {
                return "wait";

            }
        }

        map.put("msg", "用户名密码错误");
        return "puke";
    }

    @PostMapping(value = "/user/roomPoker")
    public String roomPoker(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            @RequestParam("roomPoker") String number,
                            Map<String, Object> map, Model model) {
        if (!StringUtils.isEmpty(username) && game.getPlayer().contains(username)) {

        }


        return "puke";


    }


}
