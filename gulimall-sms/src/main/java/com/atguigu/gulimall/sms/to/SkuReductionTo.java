package com.atguigu.gulimall.sms.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuReductionTo {

    private Long id;
    private Long skuId;
    private String desc;//描述
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer addOther;

    private Integer fullCount;//满几件
    //todo 这边原本数据库想设计成0.98这样，但是最后还是存了98，数据类型是decimal 这修改数据库不好改，
    //因此修改to的字段类型
    private BigDecimal discount;//打几折

    private Integer type;//满减的类型，满几件打几折，还是满xxx减xxx元；  0-打折  1-满减
}
