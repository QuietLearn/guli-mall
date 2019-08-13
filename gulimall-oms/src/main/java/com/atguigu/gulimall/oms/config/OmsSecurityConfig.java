package com.atguigu.gulimall.oms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


//@EnableWebSecurity()
@Configuration
public class OmsSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/**").permitAll();

        //csrf功能一定先干掉...
        /**
         * 	跨站请求伪造； 害怕别人给你发起的请求少带令牌
         * 		post(form表单)提交的时候都必须带一个_csrf令牌。
         * 	做这个全系统的表单都得加
         *
         * 	说白了就是(身份)认证，认证的身份才能够访问springsecurity控制的API接口，防止黑客恶意攻击
         */
        //防重复提交
        //springsecurity认为所有的post请求都要防重复提交，都要带令牌

        http.csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //web.
    }
}
