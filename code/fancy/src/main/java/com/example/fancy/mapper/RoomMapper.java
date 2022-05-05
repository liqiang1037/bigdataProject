package com.example.fancy.mapper;

import com.example.fancy.bean.Room;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
//房间
@Repository
@Mapper
public interface RoomMapper {
    Room findByName(String roomname);
    void save(Room room);
}
