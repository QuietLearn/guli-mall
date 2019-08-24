package com.atguigu.gulimall.ums.service;

import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.ums.vo.MemberLoginVo;
import com.atguigu.gulimall.ums.vo.MemberRegistVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.ums.entity.MemberEntity;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.QueryCondition;


/**
 * 会员
 *
 * @author heyijie
 * @email hyj78586421@outlook.com
 * @date 2019-08-01 19:21:59
 */
public interface MemberService extends IService<MemberEntity> {
    /**
     * 检查用户是否存在
     * @param username
     * @return
     */
    ServerResponse checkMemberExist(String username);


    PageVo queryPage(QueryCondition params);

    ServerResponse register(MemberRegistVo memberRegisterVo);
    /**
     * 门户用户 根据验证码登录
     * @param loginAccout
     * @param verificationCode
     * @return
     */
    ServerResponse loginByCode(String loginAccout, String verificationCode);

    /**
     * 发送短信
     * @param mobile 手机号
     * @param type 发送验证码类型
     * @return
     */
    ServerResponse sendSms(String mobile, String type);

    ServerResponse login(MemberLoginVo memberLoginVo);

}

