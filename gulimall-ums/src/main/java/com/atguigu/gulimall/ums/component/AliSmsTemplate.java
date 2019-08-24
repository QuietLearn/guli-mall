package com.atguigu.gulimall.ums.component;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gulimall.commons.utils.JsonUtil;
import com.atguigu.gulimall.commons.utils.RandomDataUtil;
import com.atguigu.gulimall.ums.common.state.MessageTemplateEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * 发送短信Template
 */
@ConfigurationProperties(prefix = "aliyun.sms")
@Component
@Data
@Slf4j
public class AliSmsTemplate {

    private String product;
    private String domain;
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String version;
    private String action;
    private String regionId;
    private String accessKeyId;
    private String accessSecret;

    //模板签名
    private String signName;
    //模板sms 号
    private String templateCode;

    private String messageCode;

    private Map jsonTemplate;

//    private String templateParam;
    //发送短信
    public boolean sendCodeSms(String mobile,String type,String messageCode) {
    //设置超时时间-可自行调整
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);


        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(domain);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        request.setVersion(sdf.format(new Date()));
        request.setVersion("2017-05-25");
        request.setAction(action);
        request.putQueryParameter("RegionId", regionId);
        request.putQueryParameter("PhoneNumbers", mobile);
        request.putQueryParameter("SignName", signName);

        try {
            templateCode = MessageTemplateEnum.getTemplateCode(type);
        } catch (Exception e) {
            log.error("发生异常{}",e.toString());
            return false;
        }

        request.putQueryParameter("TemplateCode", templateCode);
        this.messageCode =  messageCode;

        log.info("准备给{}发送{}短信{}",mobile,templateCode, this.messageCode);

        jsonTemplate.replace("code", this.messageCode );
        request.putQueryParameter("TemplateParam", JsonUtil.obj2Json(jsonTemplate));

        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            if (response.getHttpStatus()==200){
                return true;
            }
        } catch (Exception e){
            log.error("给手机号{} 发送短信{}出现问题，异常原因{} ",mobile,this.messageCode,e.toString());
        }
        return false;
    }


    public static void main(String[] args) {
        new AliSmsTemplate().sendCodeSms("13065708090","1000", RandomDataUtil.generateRandomNum(6));
    }
}
