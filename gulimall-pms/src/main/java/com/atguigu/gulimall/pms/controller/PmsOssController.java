package com.atguigu.gulimall.pms.controller;

import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.bean.ResponseCode;
import com.atguigu.gulimall.commons.exception.GuliException;
import com.atguigu.gulimall.pms.service.PmsOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/pms/oss/")
public class PmsOssController {

    @Autowired
    private PmsOssService pmsOssService;

    @RequestMapping("policy")
    public Resp getPolicy(){

        try {
            return pmsOssService.getPolicy();
        } catch (Exception e) {
            throw new GuliException(ResponseCode.ERROR);
        }
    }

}
