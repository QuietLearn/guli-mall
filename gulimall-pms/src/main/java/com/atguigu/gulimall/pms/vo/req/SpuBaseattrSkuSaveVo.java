package com.atguigu.gulimall.pms.vo.req;

import com.atguigu.gulimall.pms.entity.AttrEntity;
import com.atguigu.gulimall.pms.entity.SkuInfoEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel
public class SpuBaseattrSkuSaveVo implements Serializable {

    @ApiModelProperty(name = "spuName",value = "商品名称")
    private String spuName;

    @ApiModelProperty(name = "catalogId",value = "所属分类id")
    private Long catalogId;

    @ApiModelProperty(name = "brandId",value = "品牌id")
    private Long brandId;

    @ApiModelProperty(name = "publishStatus",value = "上架状态[0 - 下架，1 - 上架]")
    private Integer publishStatus;


    //这是图片信息
    @ApiModelProperty(name = "spuDescription",value = "商品描述 现在描述都以图片显示")
    private String spuDescription;

    //spu的详情图
    @ApiModelProperty(name = "spuImages",value = "spu的详情图")
    private String[] spuImages;

    @ApiModelProperty(name = "baseAttrs",value = "spu的相关基本属性值")
    private List<BaseAttrVo> baseAttrs;

    @ApiModelProperty(name = "skus",value = "sku 具体商品信息")
    private List<SkuInfoVo> skus;
}
