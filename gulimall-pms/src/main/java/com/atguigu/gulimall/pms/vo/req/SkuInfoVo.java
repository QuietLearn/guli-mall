package com.atguigu.gulimall.pms.vo.req;

import com.atguigu.gulimall.pms.entity.AttrEntity;
import com.atguigu.gulimall.pms.entity.SkuInfoEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfoVo extends SkuInfoEntity {
//    这两个SkuInfo的属性前端没给，到时候从spu里加进去
//private Long catalogId;
//	private Long brandId;
//    skuDefaultImg;

//    pms_sku_images sku图片
    private String[] images;



// 是否叠加其他优惠[0-不可叠加，1-可叠加] 其他几个表皆有
    private Integer fullAddOther;
    //    sms_sku_bounds 商品sku积分设置
    // 购物积分
    private BigDecimal buyBounds;
    //    成长积分
    private BigDecimal growBounds;
//   优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
    private Integer[] work;



    // sms_sku_full_reduction 商品满减信息
//    满多少
    private BigDecimal fullPrice;
//    减多少
    private BigDecimal  reducePrice;



    //sms_sku_ladder 商品阶梯价格
    //    满几件
    private Integer fullCount;
    //    打几折
    private BigDecimal discount;
    //    折后价
    private BigDecimal price;


    private List<SaleAttrVo> saleAttrs;

//    这个属性是以后库存中一件件加上来的，新保存1个商品信息用不着这个库存属性；
    private Integer stock;



}
