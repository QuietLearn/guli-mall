package com.atguigu.gulimall.pms.vo.resp;

import com.atguigu.gulimall.pms.entity.SkuInfoEntity;
import com.atguigu.gulimall.pms.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.pms.vo.detail.CouponsVo;
import com.atguigu.gulimall.pms.vo.detail.DetailAttrGroup;
import com.atguigu.gulimall.pms.vo.detail.DetailSaleAttrVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel
@Data
public class SkuItemDetailVo {

    //1、当前sku的基本信息
    /*@ApiModelProperty(name = "skuId",value = "skuId")
    private Long skuId;
    @ApiModelProperty(name = "spuId",value = "spuId")
    private Long spuId;

    @ApiModelProperty(name = "catalogId",value = "所属分类id")
    private Long catalogId;

    @ApiModelProperty(name = "brandId",value = "品牌id")
    private Long brandId;

    @ApiModelProperty(name = "skuTitle",value = "标题")
    private String skuTitle;

    @ApiModelProperty(name = "skuSubtitle",value = "副标题")
    private String skuSubtitle;

    @ApiModelProperty(name = "price",value = "价格")
    private BigDecimal price;

    @ApiModelProperty(name = "weight",value = "重量")
    private BigDecimal weight;*/


    private SkuInfoEntity product = new SkuInfoEntity();
    //2、sku的所有图片
    @ApiModelProperty(name = "skuDefaultImg",value = "默认图片")
    private List<String> imgList;

    //3、sku的所有促销信息
    private List<CouponsVo> coupons;

    //4、sku的所有销售属性组合
    private List<DetailSaleAttrVo> saleAttrs;

    //5、spu的所有基本属性
    private List<DetailAttrGroup> attrGroups;

    //6、详情介绍
    private SpuInfoDescEntity desc;

}
