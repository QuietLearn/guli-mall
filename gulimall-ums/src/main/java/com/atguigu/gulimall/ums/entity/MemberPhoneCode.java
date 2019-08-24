package com.atguigu.gulimall.ums.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MemberPhoneCode {
    private Integer id;

    private String phone;

    private String code;

    private String type;

    private Date gmtCreate;

    private Date gmtUpdate;
}
