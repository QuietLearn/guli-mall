package com.atguigu.gulimall.ums.util;

import com.atguigu.gulimall.ums.entity.MemberEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class JwtUtils {

    public static final String SUBJECT = "guli-user";

    //秘钥
    public static final String APPSECRET = "guli666_hyj_hh";

    //过期时间，毫秒，30分钟
    public static final long EXPIRE = 1000 * 60 * 30;

    /**
     * 生成Jwt令牌
     * @param member
     * @return
     */
    public static String generateJWT(MemberEntity member){
        Long memebrId = member.getId();
        String nickname = member.getNickname();
        String memberHeader = member.getHeader();

        if (member == null || memebrId ==null
                || StringUtils.isEmpty(nickname)
                || StringUtils.isBlank(memberHeader)) {
            return null;
        }

        String token = Jwts.builder()
//                主题，这个jwt保存什么数据，作什么功能
                .setSubject(SUBJECT)
                .claim("memebrId", memebrId)
                .claim("nickname", nickname)
                .claim("header", memberHeader)
                .claim("member",member)
//                iat：发布时间
                .setIssuedAt(new Date())
//                exp：到期时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SignatureAlgorithm.HS256, APPSECRET).compact();

        return token;
    }


    /**
     * 校验jwt
     * @param jwtToken
     * @return
     */
    public static Claims checkJWT(String jwtToken){

//        过期时间能够验证是因为是jwt的自带负载，jwt解析会先验证这些自带负载属性，jwt解析 验证通过才能使用jwt的内容
//        HMACSHA256(base64UrlEncode(header) + "." +base64UrlEncode(payload),your-256-bit-secret jwts根据该算法用密钥进行验签
//)
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(APPSECRET).parseClaimsJws(jwtToken);
        Claims claims = claimsJws.getBody();


        return claims;
    }

    public static void main(String[] args) {
        char[] aa= new char[100];
        aa[0]  = 222;
        System.out.println(aa);
        System.out.println(aa.length);
    }
}