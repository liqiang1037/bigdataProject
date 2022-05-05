package com.example.fancy.controller;

import com.example.fancy.bean.Puke;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/index")
public class LoginController {
    @GetMapping("/index")
    public String index(Model model){
        System.out.println("index....");

        List<Puke> list=new ArrayList<>();
//        list.add(new Puke(1L,"幸福哈",22));
//        list.add(new Puke(2L,"张三",23));
//        list.add(new Puke(3L,"李四",24));

        model.addAttribute("list",list);

        return "/index";
    }

}
