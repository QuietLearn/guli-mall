package com.atguigu.gulimall.pms.feign;

import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.commons.to.es.EsSkuVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface EsFeignService {

    @PostMapping("/es/spu/up")
    ServerResponse<Object> spuUp(@RequestBody List<EsSkuVo> esSkuVoList);
}
