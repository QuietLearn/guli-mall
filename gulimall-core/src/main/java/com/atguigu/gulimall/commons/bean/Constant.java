package com.atguigu.gulimall.commons.bean;

public class Constant {

    public static  final String ES_GULIMALL_INDEX = "gulimall";
    public static  final String ES_SPU_TYPE = "spu";

    public static  final String TOKENS = "guli:tokens:";
    public static  final Long TOKENS_TIMEOUT = 30L; //以分钟为单位
    public interface CategoryInfo{
        String REDIS_CACHE_PREFIX="cache:category";

    }

    public interface CartInfo {
       String CART_PREFIX = "cart:user:";
        Long UNLOGIN_CART_TIMEOUT = 30L;
    }

    public static final String FRONT_SESSION_LOGIN_MEMBER="loginUser";

    public interface memberInfo{
        String REDIS_LOGIN_MEMBER_PREFIX="member:login:info:";

        String ACCESS_TOKEN ="accessToken";

        long MEMBER_REDIS_SESSION_EXTIME=30;

        long MEMBER_REDIS_WEEK_EX=7;


        String REDIS_REGISTER_CODE_PREFIX="member:register:code:";
        String REDIS_LOGIN_CODE_PREFIX="member:login:code:";

        Integer CODE_SESSION_EXTIME=2;
    }
}
