package com.atguigu.guli.cart.to;


import lombok.Data;

import java.math.BigDecimal;

//用to，而不是用core的同一类，因为core根本动不了
//无关性，如果有人修改了core的类，那么使用该类的服务的人都要修改，而分开用to以后，只有调用该服务的人要修改，
// 而且调用服务接口获取json即可感知
@Data
public class SkuCouponTo {

    /**
     * 只需要字段名一致，因为要json-->obj 通过反射映射字段值
     * 类名无所谓
     */
    /**
     * 因为 远程调用是 同步的，所以极为耗时，用异步任务执行将大大提升并发量，节省时间
     */
    private Long skuId; //商品id

    private Long couponId; //优惠券id

    private String desc;//优惠券描述

    private BigDecimal amount;//优惠卷的金额


}
