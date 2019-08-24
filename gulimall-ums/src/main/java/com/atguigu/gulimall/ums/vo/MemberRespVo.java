package com.atguigu.gulimall.ums.vo;

import lombok.Data;

/**
 * 登录成功以后响应给前端的用户信息
 *
 * 省得以后个人信息还要再查
 */

@Data
public class MemberRespVo {

    //为什么下述 信息不放在jwt，因为jwt 只做身份验证即可，用来做后端请求验证，
    //这些信息只用来给前端做展示，放在jwt中，数据量过多，浪费流量
    private String username;
    private String email;
    private String header;
    private String mobile;
    private String sign;
    private Long levelId;
    //以上部分明文返回。


    //以下部分放在jwt中。
    //前端需要访问的令牌 //使用jwt做的。
    private String token;
    private Long memberId;
}
