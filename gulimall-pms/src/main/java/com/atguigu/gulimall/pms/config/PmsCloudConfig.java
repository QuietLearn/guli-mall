package com.atguigu.gulimall.pms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.gulimall.pms.feign")
public class PmsCloudConfig {


    /**
     * 我们的线程池
     * @param corePoolSize
     * @param maximumPoolSize
     * @return
     */
//    @ConfigurationProperties(prefix = "main.business.threadpool")
    @Bean("mainThreadPool")  //主业务线程池
    public ThreadPoolExecutor threadPoolExecutor(@Value("${main.business.threadpool.corePoolSize}") int corePoolSize,
                                                 @Value("${main.business.threadpool.maximumPoolSize}") int maximumPoolSize,
                                                 @Value("${main.business.threadpool.keepAliveTime}") long keepAliveTime){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
//                队列的大小也可以根据系统性能自定义【内存大小】
                new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE/2));

        return threadPoolExecutor;

    }
}
