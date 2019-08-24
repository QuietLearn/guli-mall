package com.atguigu.gulimall.ums.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  @ApiModel:描述返回的对象
 *
 *  每一个功能页面只用部分数据，为他单独抽取vo；
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel
public class MemberLoginRespVo {

    @ApiModelProperty(value = "访问令牌")
    private String accessToken;

    @ApiModelProperty(value = "会员的账号")
    private String loginacct;

    @ApiModelProperty("会员的昵称")
    private String username;

    @ApiModelProperty(value="邮箱地址")
    private String email;

    @ApiModelProperty(value="实名认证状态")
    private String authstatus;

    @ApiModelProperty(value="用户类型")
    private String usertype;

    @ApiModelProperty(value="真实姓名")
    private String realname;

    @ApiModelProperty(value="身份证卡号")
    private String cardnum;

    @ApiModelProperty(value="账户类型")
    private String accttype;


}