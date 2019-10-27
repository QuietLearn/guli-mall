package com.atguigu.gulimall.wms.vo;

import lombok.Data;

@Data
public class SkuLock {


        private Long skuId;
        //仓库id
        private Long wareId;
        //锁了几个
        private Integer locked;
        //是否锁库存成功
        private Boolean success;
        private String orderToken;
}
