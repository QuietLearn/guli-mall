package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.order.vo.ware.LockStockVo;
import com.atguigu.gulimall.order.vo.ware.SkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-wms")
public interface WareHourseFeignService {

    @PostMapping("/wms/waresku/checkAndLock")
    public Resp<LockStockVo> lockAndCheckStock(@RequestBody List<SkuLockVo> skuIds);
}
