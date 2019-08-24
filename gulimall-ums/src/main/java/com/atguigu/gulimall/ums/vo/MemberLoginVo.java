package com.atguigu.gulimall.ums.vo;

import lombok.Data;

@Data
public class MemberLoginVo {

    //既可以是手机，又可以是email，又可以是username
    private String loginacct;

    private String password;

    //防重复提交，以免数据库压力过大，直接在java业务服务端 拒绝
    private String code;
}
