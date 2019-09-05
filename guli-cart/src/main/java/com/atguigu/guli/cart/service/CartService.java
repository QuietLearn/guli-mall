package com.atguigu.guli.cart.service;

import com.atguigu.guli.cart.vo.CartVo;
import com.atguigu.gulimall.commons.bean.ServerResponse;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface CartService {

    ServerResponse getCartDetail(String userKey, String authorization) throws ExecutionException, InterruptedException;

    CartVo addProductToCart(Long skuId, Integer num, String userKey, String authorization) throws ExecutionException, InterruptedException;

    CartVo updateCart(Long skuId, Integer num, String userKey, String authorization);

    CartVo checkCart(Long[] skuId, Integer status, String userKey, String authorization);
}
