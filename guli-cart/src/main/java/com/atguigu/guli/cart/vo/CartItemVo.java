package com.atguigu.guli.cart.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项数据
 */

public class CartItemVo {

    @Setter  @Getter
    private Long skuId;//商品的id
    @Setter  @Getter
    private String skuTitle;//商品的标题
    @Setter  @Getter
    private String setmeal;//套餐

    @Setter  @Getter
    private String pics;//商品图片

    @Setter  @Getter
    private BigDecimal price;//单价
    @Setter  @Getter
    private Integer num;//数量

    private BigDecimal totalPrice;//商品总价

    @Setter  @Getter
    //todo 这个根据item的skuid直接去sms库中查出对应sku 的满减记录和打折记录有哪些，然后直接添加到cartitem的reductions中，
    //todo 带上会带上，至于满足不满足带上的这些满减信息的条件，还要再验证，符合就优惠
    private List<SkuFullReductionVo> reductions;//商品满减信息，包含打折满减

    @Setter  @Getter
    private List<SkuCouponVo> coupons;//优惠券

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(num+""));
    }
}

