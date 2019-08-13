package com.atguigu.gulimall.pms.vo.resp;

import com.atguigu.gulimall.pms.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel
@Data
public class AttrGroupVo implements Serializable {
    @ApiModelProperty(name = "attrId",value = "属性id")
    private Long attrId;

    @ApiModelProperty(name = "attrName",value = "属性名")
    private String attrName;

    @ApiModelProperty(name = "searchType",value = "是否需要检索[0-不需要，1-需要]")
    private Integer searchType;

    @ApiModelProperty(name = "valueType",value = "值类型[0-为单个值，1-可以选择多个值]")
    private Integer valueType;

    @ApiModelProperty(name = "icon",value = "属性图标")
    private String icon;

    @ApiModelProperty(name = "valueSelect",value = "可选值列表[用逗号分隔]")
    private String valueSelect;

    @ApiModelProperty(name = "attrType",value = "属性类型[0-销售属性，1-基本属性]")
    private Integer attrType;

    @ApiModelProperty(name = "enable",value = "启用状态[0 - 禁用，1 - 启用]")
    private Long enable;

    @ApiModelProperty(name = "catelogId",value = "所属分类")
    private Long catelogId;

    @ApiModelProperty(name = "showDesc",value = "快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整")
    private Integer showDesc;

    private AttrGroupEntity group;
}

