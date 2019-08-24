package com.atguigu.gulimall.ums;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//@Configuration  + @ComponentScan +@EnableAutoConfiguration

@EnableDiscoveryClient
@SpringBootApplication
@RefreshScope
@ComponentScan(value = {"com.atguigu.gulimall.ums","com.atguigu.gulimall.commons.handler"})
public class GulimallUmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallUmsApplication.class, args);
    }

}
