package com.example.fancy.bean;

public class PokerLog {

    private String roomname;
    private String poker;
    private String createdate;
    private String player;
    private String number;

    public PokerLog(String roomname, String poker, String createdate, String player, String number) {
        this.roomname = roomname;
        this.poker = poker;
        this.createdate = createdate;
        this.player = player;
        this.number = number;
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

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
