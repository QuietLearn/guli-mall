package com.atguigu.guli.cart.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.guli.cart.feign")
public class CartCloudConfig {


}
