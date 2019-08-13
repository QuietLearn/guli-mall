package com.atguigu.gulimall.oms.controller;

import com.atguigu.gulimall.oms.feign.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//想要动态获取配置，一个注解
@RefreshScope
public class HelloController {

    @Autowired
    WorldService worldService;

//    @Value("${test.log}")
    private String log;

    @Value("${redis-url}")
    private String redisUrl;
    /**
     * feign声明式调用
     * @return
     */
    @GetMapping("/hello")
    public String hello(){
        String msg = "";
        //远程调用gulimall-pms服务的 /world 请求对应的方法,并接受返回值
        msg = worldService.world();
        return "hello"+ msg+" "+log+" "+redisUrl;
    }
}