package com.atguigu.gulimall.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;

@RefreshScope
@SpringBootApplication
@ComponentScan(basePackages = {"com.atguigu.gulimall.commons.handler","com.atguigu.gulimall.sms"})
public class GulimallSmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSmsApplication.class, args);
    }

}
