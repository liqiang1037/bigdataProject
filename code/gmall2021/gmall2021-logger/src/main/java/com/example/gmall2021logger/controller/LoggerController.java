package com.example.gmall2021logger.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggerController {
    @RequestMapping("test")
    public String test(){
        System.out.println("123213213");
        return "success";
    }

}
