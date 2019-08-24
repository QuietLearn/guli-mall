package com.atguigu.gulimall.commons.bean;

public class Constant {

    public static  final String ES_GULIMALL_INDEX = "gulimall";
    public static  final String ES_SPU_TYPE = "spu";

    public interface CategoryInfo{
        String REDIS_CACHE_PREFIX="cache:category";
    }

    public interface CartInfo {
       String CART_PREFIX = "cart:user:";
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
