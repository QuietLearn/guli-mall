package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.commons.to.es.EsSkuVo;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/es")
public class SpuToEsController {

    @Autowired
    JestClient jestClient;

    /**
     * 商品上架
     * @return
     */
    @PostMapping("/spu/up")
    public ServerResponse<Object> spuUp(@RequestBody List<EsSkuVo> esSkuVoList){
        esSkuVoList.forEach(esSkuVo -> {
            //builder的source属性会通过build传给 Index的this.payload = builder.source;
            //是否决定es的类型 是根据spu商品具体类型 分开
            //index行为需要决定插入的es的index和type，因为插入id可以是es生成的，也可以是自定的
//            这边自定义为spu每个上架的sku 的id
            Index index = new Index.Builder(esSkuVo)
                    .index(Constant.ES_GULIMALL_INDEX)
                    .type(Constant.ES_SPU_TYPE)
                    .id(esSkuVo.getSkuId().toString())
                    .build();

            try {
                jestClient.execute(index);
            } catch (IOException e) {
                log.error("发生异常",e);
            }

        });

        return ServerResponse.createBySuccessMessage("es保存sku上架信息 成功");
    }
}
