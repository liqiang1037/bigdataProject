package com.example.fancy.controller;

import com.example.fancy.bean.Card;
import com.example.fancy.bean.Cards;
import com.example.fancy.bean.Puke;
import com.example.fancy.service.PlayDemo;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/puke2")
public class PukeController2 {



    @PostMapping(value = "/reloadpuke")
    @ResponseBody
    public String login(@RequestBody String data,Map<String, Object> map, Model model) {
        System.out.println(data);
        return "msg:/image/黑桃7.png";
    }

}
