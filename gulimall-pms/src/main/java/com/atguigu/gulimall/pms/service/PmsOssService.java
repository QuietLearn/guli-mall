package com.atguigu.gulimall.pms.service;

import com.atguigu.gulimall.commons.bean.Resp;

import javax.servlet.ServletException;
import java.io.IOException;

public interface PmsOssService {

    Resp getPolicy() throws ServletException, IOException ;
}
