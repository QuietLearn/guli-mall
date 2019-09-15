package com.atguigu.guli.cart.vo;

import lombok.Data;

import java.util.List;

@Data
public class ClearCartSkuVo {

    private List<Long> skuIds;
    private Long userId;
}
