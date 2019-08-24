package com.atguigu.gulimall.commons.to.pms;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SkuInfoVo {


    private Long skuId;//商品的id
    private String skuTitle;//商品的标题
    private String setmeal;//套餐

    private String pics;//商品图片

    private BigDecimal price;//单价



//    private List<SkuFullReductionVo> reductions;//商品满减信息，包含打折满减

//    private List<SkuCouponVo> coupons;//优惠券



}
