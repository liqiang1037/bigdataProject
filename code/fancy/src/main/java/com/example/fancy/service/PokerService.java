package com.example.fancy.service;


import com.example.fancy.mapper.PokerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service

public class PokerService {
    @Autowired
    PokerMapper pokerMapper;

    public String showPokerList(String roomname) {
        String list = pokerMapper.findByName(roomname);
        return list;
    }

    public void savePoker(String roomname,String poker,String createdate) {

        pokerMapper.save(roomname,poker,createdate);
    }


}
