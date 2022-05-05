package com.example.fancy.service;

import com.example.fancy.bean.Card;
import com.example.fancy.bean.Cards;
import com.example.fancy.bean.Room;
import com.example.fancy.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PokerRoomService {
    @Autowired
    RoomMapper roomMapper;
    @Autowired
    PokerService pokerService;

    @Autowired
    PlayService playService;

    public String centerRoom(String roomname,String name,Model model) {
        String view="wait";
        Room userExistN = roomMapper.findByName(roomname);
        if (userExistN != null) {
            view = playService.play(roomname,name , model);
        }
        return  view;
    }

    public String regist(Room user, Model model) {
        String view = "wait";
        try {
            String name=user.getName();
            String roomname=user.getRoomname();
            Room room = roomMapper.findByName(roomname);

            if (room == null) {

                roomMapper.save(user);
                Cards cards = new Cards();
                //(2)展示新的扑克牌
                cards.showCards();
                //(3)洗牌
                cards.shufCards();
                List<Card> cardList = cards.getList();
                StringBuffer str = new StringBuffer();
                int i = 0;
                for (Card card : cardList) {
                    if (i == cardList.size() - 1) {
                        str.append(card.getColor()).append(",").append(card.getNumber());
                    } else {
                        str.append(card.getColor()).append(",").append(card.getNumber()).append(":");

                    }
                    i++;
                }
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String createdate = formatter.format(date);
                String poker = str.toString();
                //创建新的扑克 与房间绑定
                pokerService.savePoker(roomname, poker, createdate);
                //庄家发牌
                view = playService.play(roomname, name, model);

            }else{
                view = playService.play(roomname, name, model);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

}
