package com.atguigu.gulimall.pms.vo.detail;

import lombok.Data;

@Data
public class DetailSaleAttrVo {

    private Long attrId;//销售属性的id
    private String attrName;//销售属性的名字
    //todo ? sale属性跟的是sku，1个sku怎么会有多个值
    //todo spu才有多个值，sale属性有图片，可以考虑用json封装{"xx":"value-picurl"}
    private String[] attrValues;



}
