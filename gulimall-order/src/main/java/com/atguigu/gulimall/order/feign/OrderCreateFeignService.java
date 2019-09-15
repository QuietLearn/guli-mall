package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.order.vo.order.OrderEntityVo;
import com.atguigu.gulimall.order.vo.order.OrderFeignSubmitVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单的所有业务。crud，正常业务
 */
@FeignClient("gulimall-oms")
public interface OrderCreateFeignService {

    @PostMapping("/oms/order/createAndSave")
    public Resp<OrderEntityVo> createAndSaveOrder(@RequestBody OrderFeignSubmitVo vo);

}
