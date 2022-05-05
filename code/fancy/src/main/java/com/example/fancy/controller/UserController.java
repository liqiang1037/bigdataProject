package com.example.fancy.controller;


import com.example.fancy.bean.User;
import com.example.fancy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequestMapping("/user1")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ModelAndView login(User user){

        return  userService.login(user);

    }

    @PostMapping("/regist")
    public String regist(User user){
        return userService.regist(user);
    }

    /**
     * 解决查询数据库中文出现乱码问题
     * @return
     */
    @RequestMapping(value = "/alluser",method = RequestMethod.GET,produces = "application/json;charset=UTF-8" )
    public List<User> findAll(){
        return userService.findAll();
    }
}

