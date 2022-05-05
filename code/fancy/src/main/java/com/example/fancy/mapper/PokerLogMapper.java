package com.example.fancy.mapper;

import com.example.fancy.bean.PokerLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface PokerLogMapper {
    String findByName(@Param("roomname")String roomname, @Param("player")String player, @Param("number") String number);
    void save(PokerLog pokerLog);
}
