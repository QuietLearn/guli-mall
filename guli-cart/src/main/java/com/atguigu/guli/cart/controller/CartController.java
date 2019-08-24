package com.atguigu.guli.cart.controller;

import com.atguigu.guli.cart.service.CartService;
import com.atguigu.gulimall.commons.bean.ServerResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "购物车系统")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 以后购物车的所有操作，前端带两个令牌
     * 1）、登录以后的jwt放在请求头的 Authorization 字段（不一定带）
     * 2）、只要前端有收到服务器响应过一个userKey的东西，以后保存起来，访问所有请求都带上；（不一定）
     * @return
     */
    //todo 第一次进来服务端会给user-key 到浏览器
    @ApiOperation("获取购物车中的数据")
    @GetMapping("/list")
    public ServerResponse getCartDetail(String userKey ,
                                        @RequestHeader(name = "Authorization",required = false) String authorization) {

        return cartService.getCartDetail(userKey,authorization);
    }


    @PostMapping("/add")
    public ServerResponse addProductToCart(@RequestParam(name = "skuId",required = true) Long skuId,
                                           @RequestParam(name = "num",defaultValue = "1") Integer num,
                                           String userKey,
                                           @RequestHeader(name = "Authorization",required = false) String authorization){

        //1、判断是否登录了
        Map map = cartService.addProductToCart(skuId, num, userKey, authorization);

        return ServerResponse.createBySuccess("添加购物车成功",map);
    }


}
