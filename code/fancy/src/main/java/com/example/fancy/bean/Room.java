package com.example.fancy.bean;

public class Room {
    //房间人数
    private String number;
    //房间名称
    private String  roomname;

    public Room(String number, String roomname, String name, String player) {
        this.number = number;
        this.roomname = roomname;
        this.name = name;
        this.player = player;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }


    //庄家
    private String  name;

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    //闲家
    private String  player;


    @Override
    public String toString() {
        return "Room{" +
                "number=" + number +
                ", name='" + name + '\'' +
                '}';
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
