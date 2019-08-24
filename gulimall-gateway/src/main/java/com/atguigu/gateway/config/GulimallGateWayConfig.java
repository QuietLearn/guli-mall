package com.atguigu.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GulimallGateWayConfig {

    /**
     * Gateway；
     *  Reactive；  Webflux；
     * 与springmvc的filter是不一样的
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter(){

        //跨域的配置
        CorsConfiguration config = new CorsConfiguration();
        //允许所有方法都能跨域
        config.addAllowedMethod("*");
        //允许所有前端服务器ajax跨域，后端服务器对任何前端请求都会加允许跨域
        config.addAllowedOrigin("*");
        //允许所有头
        config.addAllowedHeader("*");
        //cookie要做安全认证，允许携带cookie跨域，否则带了cookie的请求不要
        config.setAllowCredentials(true);//允许带cookie的跨域


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**",config);

        CorsWebFilter filter = new CorsWebFilter(source);

        return filter;
    }

    /**
     * 全局过滤器或者gatewayfilterfactory都是在容器中添加的时候有序的。
     *
     * 如果我们的过滤器顺序太低，导致上一个放行不过就来不到这里。
     * @return
     *
     * @bean
     */
//
}
