package com.atguigu.gulimall.ums.common.state;

import com.atguigu.gulimall.commons.bean.Constant;
import org.apache.commons.lang3.StringUtils;

public enum MessageTemplateEnum {
    //注册
    SIGN_IN("SMS_168340097","1000", Constant.memberInfo.REDIS_REGISTER_CODE_PREFIX),
    //登录
    LOGIN("SMS_168346045","1001",Constant.memberInfo.REDIS_LOGIN_CODE_PREFIX);

    //模板CODE
    private String code;
    //模板名称
    private String type;

    private String redisCodePrefix;

    MessageTemplateEnum(String code, String type,String redisCodePrefix) {
        this.code = code;
        this.type = type;
        this.redisCodePrefix = redisCodePrefix;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRedisCodePrefix() {
        return redisCodePrefix;
    }

    public void setRedisCodePrefix(String redisCodePrefix) {
        this.redisCodePrefix = redisCodePrefix;
    }

    public static String getRedisCodePrefix(String type){
        if (StringUtils.isBlank(type))
            throw new RuntimeException("您的参数有误");
        for (MessageTemplateEnum value : MessageTemplateEnum.values()) {
            if (value.getType().equals(type)){
                return value.getRedisCodePrefix();
            }
        }
        throw new RuntimeException("未找到匹配的RedisCodePrefix");
    }


    public static String getTemplateCode(String type){
        if (StringUtils.isBlank(type))
            throw new RuntimeException("您的参数有误");
        for (MessageTemplateEnum value : MessageTemplateEnum.values()) {
            if (value.getType().equals(type)){
                return value.getCode();
            }
        }
        throw new RuntimeException("未找到匹配的TemplateCode");
    }
}
