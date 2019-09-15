package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.order.vo.Order;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;

public interface OrderService {
    Order createOrder();

    OrderConfirmVo confirmOrderData(Long id);


    Resp<Object> submitOrder(OrderSubmitVo vo, Long userId);


}
