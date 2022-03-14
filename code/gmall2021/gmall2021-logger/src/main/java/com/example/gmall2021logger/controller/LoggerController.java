package com.example.gmall2021logger.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LoggerController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping("test1")
    public String test1() {
        System.out.println("123213213");
        return "success";
    }

    @RequestMapping("test2")
    public String test2(@RequestParam("name") String name, @RequestParam("age") int age) {
        System.out.println("name===>" + name + "  " + "age===>" + age);
        return "success";
    }

    @RequestMapping("applog")
    public String getLogger(@RequestParam("param") String jsonStr) {
        log.info(jsonStr);
        kafkaTemplate.send("ods_base_log", jsonStr);
        return "success";
    }
}
