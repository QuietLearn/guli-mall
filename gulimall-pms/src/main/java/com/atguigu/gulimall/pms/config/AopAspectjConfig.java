package com.atguigu.gulimall.pms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

//表示开启AOP代理自动配置，如果配@EnableAspectJAutoProxy表示使用cglib进行代理对象的生成；
// 设置@EnableAspectJAutoProxy(exposeProxy=true)表示通过aop框架暴露该代理对象，aopContext能够访问.
//SpuInfoServiceImpl  SpuInfoService proxy = (SpuInfoService) AopContext.currentProxy();
//https://blog.csdn.net/pml18710973036/article/details/61654277
@Configuration
@EnableAspectJAutoProxy(exposeProxy=true)
public class AopAspectjConfig {
}
