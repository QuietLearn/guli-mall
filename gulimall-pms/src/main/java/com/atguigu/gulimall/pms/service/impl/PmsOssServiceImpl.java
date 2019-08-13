package com.atguigu.gulimall.pms.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.common.utils.DateUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.utils.DateUtils;
import com.atguigu.gulimall.pms.service.PmsOssService;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Service
@ConfigurationProperties(prefix = "aliyun.oss.file")
public class PmsOssServiceImpl implements PmsOssService {
    private String accessId;
    private String accessKey;
    private String endpoint;
    private String bucket;
//    https://null.null 的原因
//    那肯定是对象先反射创建，那么随之属性是首先被初始化的，然后扫描到该类有@ConfigurationProperties来再初始化属性，属性被重新赋值
//    那麽属性已经初始化为https://null.null 了，没有重新赋值
//    private String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint

    // callbackUrl为上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
    String callbackUrl = "http://88.88.88.88:8888";

    public Resp getPolicy() throws ServletException, IOException {
        String dir = new DateTime().toString(DateUtils.DATE_PATTERN); // 用户上传文件时指定的前缀。
        String host = "https://" + bucket + "." + endpoint;
        OSSClient client = new OSSClient(endpoint, accessId, accessKey);

        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));



            return Resp.ok(respMap);

        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        }
        return Resp.fail("获取签名失败");
    }
}
