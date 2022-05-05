package com.example.fancy.service;

import com.example.fancy.bean.PokerLog;
import com.example.fancy.mapper.PokerLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PokerLogService {
    @Autowired
    PokerLogMapper pokerLogMapper;
    public String showPoker(String roomname,String player,String number) {
        String poker = pokerLogMapper.findByName(roomname,player,number);
        return poker;
    }

    public void savePoker(PokerLog pokerLog) {

        pokerLogMapper.save(pokerLog);
    }
}
