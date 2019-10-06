package com.atguigu.gulimall.order.feign;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class MyFeignConfig {


    /**
     * 解决远程调用确实授权请求头问题， 当订单确认请求进来，远程调用购物车选中商品 发起新的请求，
     * 这个拦截器就可以拦截发起 对应处理的请求头
     *
     * //没有合适的都可以自实现的，spring提供了这样的可扩展性，因为通过实现接口的多态性
     * @return
     */
    /**
     * 调用远程接口的时候feign拦截器要拦截
     * @return
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){


//        1. 通过获取前一个controller请求的所有请求头信息，拦截，然后全部设置在请的远程请求的请求信息里面
//  注意：因为RequestContextHolder用的ThreadLocal同步请求
//  2. 同步可以获取同一个线程请求的请求信息，但是订单确认请求方法是异步线程【远程调用】，所以获取不到不同的线程的请求信息
        return new RequestInterceptor() {
            @Override
            /**
             * template是后面远程调用接口的请求
             */
            public void apply(RequestTemplate template) {
                System.out.println("拦截器的线程号..."+Thread.currentThread().getId());
                //拿到原生请求，
                //RequestContextHolder利用经典写法
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                //这是原来controller过来的请求, RequestTemplate是新的feign请求
                HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");
                System.out.println("拦截器获取到的内容...."+authorization);
                //这样远程接口也就可以带上相同的请求头信息了，请求头里的一些自定义字段
                template.header("Authorization",authorization);
            }
        };
    }
}
