package com.atguigu.lock.test.controller;

import com.atguigu.lock.test.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @Autowired
    RedisService redisService;
    @GetMapping("/incr")
    public String incr() throws InterruptedException {
        //redisService.incr();
        redisService.incr2();
        return "ok";
    }


    @GetMapping("/read")
    public String readValue() throws InterruptedException {
        return redisService.read();
    }


    @GetMapping("/write")
    public String writeValue() throws InterruptedException {
        return redisService.write();
    }

    @GetMapping("/lockdoor")
    public String suomen() throws InterruptedException {

        String lockdoor = redisService.lockdoor();
        return lockdoor;
    }

    @GetMapping("/go")
    public String gogogo(){
        redisService.go();
        return "溜了...";
    }




}
