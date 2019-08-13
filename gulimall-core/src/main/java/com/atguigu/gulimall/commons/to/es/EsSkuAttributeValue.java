package com.atguigu.gulimall.commons.to.es;

import lombok.Data;

/**
 * 老师说只有基本属性即可，因为标题中包含了销售属性，所以不必检索
 * 个人感觉应该基本属性和销售属性都可以检索，颜色不必了，应为是所有sku都显示的，可以滑动选择产品
 * 滑动选择只是选择颜色，所以还是有 区别的，
 *
 * todo
 * 难在jd是这样设计的，平时因为需要吸引留住用户，所以给用户显示的中间价格的sku商品【spu1个段的sku应该有各个颜色的sku】
 * 然后选择销售条件时，才给你在es中 检索筛选出对应条件的sku信息的段显示出来【该段的所有颜色应该都有】
 */
@Data
public class EsSkuAttributeValue {
    //商品和属性关联的数据表的主键id
    private Long productBaseAttrValueId;
    //这个属性关系对应的spu的id
    private Long spuId;
    //当前属性对应的sku id
    private Long skuId;
    //当前sku对应的属性的attr_id
    private Long attrId;
    //属性名  电池
    private String attrName;
    //3G   3000mah
    private String attrValue;
}
