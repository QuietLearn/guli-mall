package com.atguigu.gulimall.commons.to.es;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * 以sku为单位存在es中；
 *
 * 1）、sku的基本信息
 * 2）、sku的品牌，分类，等...
 * 3）、sku的检索属性信息；
 *
 * 这是后端自己存的
 */
@Data
public class EsSkuVo {

    //todo 决定是否要变成id
    //skuId
    private Long skuId;

    private Long spuId;
    //商品码
   /* private String skuCode;
    @ApiModelProperty(name = "skuName",value = "sku名称")
    private String skuName;
    @ApiModelProperty(name = "skuDesc",value = "sku介绍描述")
    private String skuDesc;*/

    @ApiModelProperty(name = "catalogId",value = "所属分类id")
    //todo 决定是否要变成categoryId;
    private Long productCategoryId;
    //sku的分类名字
    private String productCategoryName;

    @ApiModelProperty(name = "brandId",value = "品牌id")
    private Long brandId;
    //品牌名
    private String brandName;

    //todo sku的默认图片 pic
    @ApiModelProperty(name = "skuDefaultImg",value = "默认图片")
    private String skuDefaultImg;

    //todo name;//这是需要检索的sku的标题
    @ApiModelProperty(name = "skuTitle",value = "标题")
    private String skuTitle;

    /*@ApiModelProperty(name = "skuSubtitle",value = "副标题")
    private String skuSubtitle;*/

    @ApiModelProperty(name = "price",value = "价格")
    private BigDecimal price;

    //销量，用于es排序
    private Integer sale;//sku-sale 销量
    //库存，用于es排序
    private Integer stock;//sku-stock 库存
    // 综合排序分 //排序分 热度分
    private Integer sort;
    //根据商品上线时间(新品)，用于es排序
    private Date gmtCreate;


    //保存当前sku所有需要检索的属性；
    //检索属性来源于sku的全部属性【基本属性，销售属性】中的search_type=1
    private List<EsSkuAttributeValue> attrValueList;//检索属性
}
