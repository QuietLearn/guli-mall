package com.atguigu.gulimall.pms.feign;

import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.commons.to.SkuSaleInfoTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-sms")
public interface SmsSkuMarketService {

    @PostMapping("/sms/skubounds/saleinfo/save")
    ServerResponse saveSkuSaleInfos(@RequestBody List<SkuSaleInfoTo> skuSaleInfoToList);
}
