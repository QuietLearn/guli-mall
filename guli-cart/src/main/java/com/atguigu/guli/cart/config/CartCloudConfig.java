package com.atguigu.guli.cart.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.guli.cart.feign")
public class CartCloudConfig {

    //max 看当前业务 并发量高不高，高，最大线程数一定要调大，提高最大同时容忍的请求数
    //但请注意不要把内存耗尽，8--->涨到1000，空闲时线程会缩到8，动态伸缩的
    //keepAliveTime 伸缩时间，距离上次请求已经有多少时间没有新请求需要这个线程，就干掉这个线程
    //0代表没人用就直接干掉，不用等时间
    @Bean("mainExecutor")
    @Primary  //默认就是他
    public ThreadPoolExecutor mainThreadPoolExecutor(){

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 1000,
                0l, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE / 2));
//        Queue(无界队列 | 如果不改默认大小是Integer.maxValue) ，高并发时，很可能队列也会挤爆，到时用拒绝策略 要么丢弃要么让它等待
        return threadPoolExecutor;
    }

    //核心业务与非核心业务线程池
    //异步任务
    //如果有一些其他耗时任务，但不是给用户体验，是用来后台收集数据的，如日志，打包的东西耗时的任务都交给异步任务处理
    //等以后系统压力大了，可以直接关掉非核心业务线程池，给核心业务线程池腾一些资源


    @Bean("otherExecutor")
    public ThreadPoolExecutor noMainThreadPoolExecutor(){
        //cpu核；
        //无界队列；
        ThreadPoolExecutor executor = new ThreadPoolExecutor(8, 1000, 0L,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(Integer.MAX_VALUE / 2));

        return executor;
    }
}
