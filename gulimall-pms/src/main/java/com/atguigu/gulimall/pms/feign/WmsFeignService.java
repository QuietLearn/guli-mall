package com.atguigu.gulimall.pms.feign;

import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.commons.to.wms.SkuStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-wms")
public interface WmsFeignService {
    //@RequestBody不用  写参数名，因为序列化  反序列化根据反射自动创建，然后看能不能属性名是否相同反射set进去即可

//    我是真的蠢，requestbody的只能用posy
    @PostMapping("/wms/waresku/skus/stock")
    ServerResponse<List<SkuStockVo>> skuWareInfos(@RequestBody List<Long> skuIds);
}
