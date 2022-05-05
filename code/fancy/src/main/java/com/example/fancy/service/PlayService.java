package com.example.fancy.service;

import com.example.fancy.bean.Card;
import com.example.fancy.bean.Player;
import com.example.fancy.bean.PokerLog;
import com.example.fancy.bean.Room;
import com.example.fancy.mapper.PokerMapper;
import com.example.fancy.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class PlayService {
    @Autowired
    RoomMapper roomMapper;
    @Autowired
    PokerMapper pokerMapper;
    @Autowired
    PokerLogService pokerLogService;

    static List<Card> cardList=new ArrayList<>();
    static  int number=0;

    //创建玩家
    public List<Player> setPlayer(String roomname) {
        List<Player> p = new ArrayList();
        Room room = roomMapper.findByName(roomname);
        Player banker = new Player(room.getName());
        p.add(banker);
        String player = room.getPlayer();
        String[] playerList = player.split(",");
        if (playerList.length > 0) {
            for (String pstr : playerList) {
                p.add(new Player(pstr));
            }
        }
        return p;

    }


    public void dealCard(String roomname, String flag,String num) {
        //设置玩家
        List<Player> p = setPlayer(roomname);
        // 获取牌
        if(flag.equals("1")){
            if(num.equals("1")) {
                String poker = pokerMapper.findByName(roomname);
                String[] cardsList = poker.split(":");
                for (String s : cardsList) {
                    String colo = s.split(",")[0];
                    String number = s.split(",")[1];
                    cardList.add(new Card(colo, number));
                }
            }
            if (cardList.size() > p.size()) {

                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String createdate = formatter.format(date);
                for (int j = p.size() - 1; j >= 0; j--) {
                    Card card = cardList.get(j);
                    p.get(j).setHandCards(card);
                    cardList.remove(j);
                    //将牌存入数据库
                    String playerpoker="/image/" + card.getColor() + card.getNumber() + ".png";
                    String player=p.get(j).getName();
                    PokerLog pokerLog =new PokerLog(roomname,playerpoker,createdate,player,num);
                    pokerLogService.savePoker(pokerLog);

                }

                Card card = cardList.remove(0);
                String playercommon="/image/" + card.getColor() + card.getNumber() + ".png";
                PokerLog pokerLog =new PokerLog(roomname,playercommon,createdate,"common",num);
                pokerLogService.savePoker(pokerLog);


            }else{
                cardList.clear();
                number=0;
            }
        }


    }

    public String play(String roomname, String name,Model model) {
        String flag = "";
        Room room = roomMapper.findByName(roomname);
        String roomStr = room.getName();
        if (name.equals(roomStr)) {
            flag = "1";
            number++;
           dealCard(roomname,flag,String.valueOf(number));
        }
        String showPoker = pokerLogService.showPoker(roomname, name, String.valueOf(number));
        String common = pokerLogService.showPoker(roomname, "common", String.valueOf(number));

        if (showPoker != null) {
            //获取牌
                model.addAttribute("common", common);//普通字符串
                model.addAttribute("poker", showPoker);//储存Map
                model.addAttribute("name", name);//普通字符串
                model.addAttribute("sum", room.getNumber());//普通字符串
                model.addAttribute("count", cardList.size());

        }else{
            if(number==0) {
                return "wait";
            }else{
                return "over";
            }
        }

        return "index2";
    }

}