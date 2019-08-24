package com.atguigu.guli.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;


@RefreshScope

@ComponentScan({"com.atguigu.guli.cart","com.atguigu.gulimall.commons.handler"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GuliCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuliCartApplication.class, args);
    }

}
