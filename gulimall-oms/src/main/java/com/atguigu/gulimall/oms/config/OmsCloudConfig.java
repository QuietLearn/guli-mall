package com.atguigu.gulimall.oms.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDiscoveryClient  //开启服务注册发现功能
//直接把feign客户端扫描进来，并做动态代理加载到spring容器中
@EnableFeignClients(basePackages = "com.atguigu.gulimall.oms.feign") //开启feign的远程调用功能。
//配置feign接口所在的包
public class OmsCloudConfig {


}