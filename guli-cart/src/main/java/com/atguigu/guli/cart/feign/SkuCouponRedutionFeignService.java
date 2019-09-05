package com.atguigu.guli.cart.feign;

import com.atguigu.guli.cart.to.SkuCouponTo;
import com.atguigu.guli.cart.vo.SkuFullReductionVo;
import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.bean.ServerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-sms")
public interface SkuCouponRedutionFeignService {

    @GetMapping("/sms/sku/coupon/{skuId}")
    public ServerResponse<List<SkuCouponTo>> getCoupons(@PathVariable("skuId") Long skuId);

    @GetMapping("/sms/sku/reduction/{skuId}")
    public ServerResponse<List<SkuFullReductionVo>> getRedutions(@PathVariable("skuId") Long skuId);
}
