package com.atguigu.gateway.filter;

import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.utils.GuliJwtUtils;
import com.atguigu.gulimall.commons.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证过滤器
 * 1、写一个全局的网关的过滤器
 * 2、把他加入到容器中
 *
 *
 * 3、只有能访问的请求才拦截
 *
 */
//@Order(1)
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GuliAutheticationFilter implements GlobalFilter {


    @Autowired
    StringRedisTemplate redisTemplate;
    /**
     * Webflux的编程方法，流式编程；
     * @param exchange
     * @param chain
     * @return
     *
     * doFilter(req,resp,filterChain){
     *
     * }
     */
//    spring网关给我们暴露的过滤器，相当于给我们封装的东西，
//    接口是GlobalFilter

    /**
     * 带令牌了我就验证，不带就不验
     *  实际方法会对这个token好好验证，只是做一个预置的拦截
     *  todo 我觉的还是要将实际的方法隔开，但是因为网关 设置需要登陆的验证请求 的 全局验证拦截器
     *  todo 全局拦截器对 整个大项目的所有请求做拦截？？ 这要隔开的方法也太多了，不像 geely项目那样只有没几个
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("网关全局令牌验证开始.....");

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        HttpHeaders headers = request.getHeaders();
        List<String> authorizationList = headers.get("Authorization");
        if (!CollectionUtils.isEmpty(authorizationList)){
            String authorization = authorizationList.get(0);
            log.info("jwt 验证令牌:{}",authorization);

            Map<String, Object> jwtBody = null;
            try {
                jwtBody = GuliJwtUtils.getJwtBody(authorization);
                //验证令牌通过，放行了
                Integer memberId = (Integer) jwtBody.get("memberId");
                //自动进行redis的数据续期
                //{"id":1,"token":"b7c1ce9e434a4827b495c191e943aec6"}
                String token = (String) jwtBody.get("token");

                String memberPrefix = Constant.memberInfo.REDIS_LOGIN_MEMBER_PREFIX + token;

                String memberStr = redisTemplate.opsForValue().get(memberPrefix);
                if (StringUtils.isEmpty(memberStr)){
                    log.error(memberId+"用户信息已过期");
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return response.setComplete();
                }
                //自动续期
                redisTemplate.expire(memberPrefix,Constant.memberInfo.MEMBER_REDIS_WEEK_EX,TimeUnit.DAYS);

                return chain.filter(exchange);
            } catch (Exception e) {
                log.error("jwt验签失败，该请求拒绝，身份验证不通过",e);
                //设置403状态。
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }


        }

        log.info("网关全局令牌验证结束.....");
        return chain.filter(exchange);

    }
}
