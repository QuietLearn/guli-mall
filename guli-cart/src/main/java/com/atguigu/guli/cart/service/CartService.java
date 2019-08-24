package com.atguigu.guli.cart.service;

import com.atguigu.guli.cart.vo.CartVo;
import com.atguigu.gulimall.commons.bean.ServerResponse;

import java.util.Map;

public interface CartService {

    ServerResponse getCartDetail(String userKey, String authorization);

    Map addProductToCart(Long skuId, Integer num, String userKey, String authorization);
}
