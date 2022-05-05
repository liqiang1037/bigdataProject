package com.example.fancy.bean;

public class Poker {
    private String roomname;
    private String poker;
    private String createdate;

    public Poker(String roomname, String poker, String createdate) {
        this.roomname = roomname;
        this.poker = poker;
        this.createdate = createdate;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public String getPoker() {
        return poker;
    }

    public void setPoker(String poker) {
        this.poker = poker;
    }

    public String getCreatedate() {
        return createdate;
    }

    public void setCreatedate(String createdate) {
        this.createdate = createdate;
    }
}
