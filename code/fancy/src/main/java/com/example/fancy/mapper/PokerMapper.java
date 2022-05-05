package com.example.fancy.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

//每一个房间 一副牌
@Repository
@Mapper
public interface PokerMapper {
    String findAll();
    String findByName(String roomname);
    void save(@Param("roomname")String roomname,@Param("poker") String card, @Param("createdate")String createdate);
}
