package com.example.fancy.controller;

import com.example.fancy.bean.Room;
import com.example.fancy.bean.User;
import com.example.fancy.service.PokerRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/user2")
public class RoomController {
    @Autowired
    PokerRoomService pokerRoomService;

    @PostMapping("/registRoom")
    public ModelAndView regist(Room room ,Model model ) {
        String view=pokerRoomService.regist(room,model);
        return new ModelAndView(view);

    }

    @PostMapping("/centerRoom")
    public ModelAndView center(String  roomname,String name ,Model model ) {
        String view=pokerRoomService.centerRoom(roomname,name,model);
        return new ModelAndView(view);

    }
}
