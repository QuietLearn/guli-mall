package com.atguigu.gulimall.commons.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Map;

public class GuliJwtUtils {

    /**
     * 秘钥....
     */
    private static String key = "guli666_hyj_hh";

    /**
     * 自动在jwt util中实现对beareaPrefix 前缀的解析
     */
    private static String beareaPrefix = "Bearer ";

    public static final String SUBJECT = "guli-user";
    //过期时间，毫秒，30分钟
    public static final long EXPIRE = 1000 * 60 * 30;
    /**
     *
     * @param payload  自定义的负载内容
     * @param claims   jwt默认支持的属性
     * @return
     */
    public static String buildJwt(Map<String,Object> payload, Claims claims){

        JwtBuilder builder = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, GuliJwtUtils.key)
                .setClaims(payload);//设置自定义的负载

        if(claims!=null){
            if(claims.getId()!=null){
                builder.setId(claims.getId());
            }
            if(claims.getAudience()!=null){
                builder.setAudience(claims.getAudience());
            }
            if(claims.getExpiration()!=null){
                builder.setExpiration(claims.getExpiration());
            }
            if (claims.getNotBefore()!=null){
                builder.setNotBefore(claims.getNotBefore());
            }
            //xxxxx
        }

        String compact = builder.compact();
        return beareaPrefix+ compact;
        //Bearer dsadasdada
    }

    /* map.put("memberId",member.getId());
        map.put("nickName",member.getNickname());
        map.put("header",member.getHeader());
        map.put("token",token);*/


    public static void checkJwt(String jwt){

        //去掉Bearer 前缀
        String substring = jwt.substring(beareaPrefix.length());

        //解出来的串才是jwt
        Jwts.parser().setSigningKey(key).parse(substring);


    }

    public static Map<String,Object> getJwtBody(String jwt){
        String substring = jwt.substring(beareaPrefix.length());

        return Jwts.parser().setSigningKey(key).parseClaimsJws(substring).getBody();
    }

}
