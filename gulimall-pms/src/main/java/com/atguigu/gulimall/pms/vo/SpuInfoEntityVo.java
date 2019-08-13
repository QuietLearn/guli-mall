package com.atguigu.gulimall.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@ApiModel
@Data
public class SpuInfoEntityVo implements Serializable {
    private Long id;
    @ApiModelProperty(name = "spuName",value = "商品名称")
    private String spuName;
//    商品描述
    private String spuDescription;
    private Long catalogId;
    private String catagoryName;
    // 品牌id
    private Long brandId;
    //上架状态[0 - 下架，1 - 上架]
    private Integer publishStatus;

    private Date gmtCreate;
    private Date gmtModified;
}
