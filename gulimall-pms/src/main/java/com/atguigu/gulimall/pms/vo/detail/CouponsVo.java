package com.atguigu.gulimall.pms.vo.detail;

import lombok.Data;

@Data
public class CouponsVo {

    // 0-优惠券    1-满减    2-阶梯
    private Integer type;

    private String name;//促销信息/优惠券的名字

    private String desc;
}
