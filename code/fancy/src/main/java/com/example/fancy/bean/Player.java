package com.example.fancy.bean;

import java.util.ArrayList;

import java.util.List;

public class Player {


    private String name;

    private List handCards = new ArrayList();

    public Player( String name){


        this.name = name;

    }

    public List getHandCards() {

        return handCards;

    }

    public void setHandCards(Card card) {

        handCards.add(card);

    }



    public String getName() {

        return name;

    }



}
