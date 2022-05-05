package com.example.fancy.controller;


import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @ClassName HelloController
 * @Description TODO
 * @Author Rock-PC5
 * @Date 2018/9/13 9:02
 * @Version 1.0
 **/
@RestController
@CrossOrigin
public class AjaxController {

    //模拟用户登录验证
    @RequestMapping(value = "/login/{username}/{password}",method = RequestMethod.GET)
    public Object login(@PathVariable String username,@PathVariable String password){
        System.out.println(username);
        System.out.println(password);
        return "success";
    }

    //模拟现实用户信息
    @RequestMapping(value = "/list")
    public Object list(){
        String username = "root";
        String password = "123456";
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>(1);
        objectObjectHashMap.put("username",username);
        objectObjectHashMap.put("password",password);
        return objectObjectHashMap;
    }
}