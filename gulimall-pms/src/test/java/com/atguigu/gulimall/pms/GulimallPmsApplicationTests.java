package com.atguigu.gulimall.pms;

import com.atguigu.gulimall.pms.dao.ProductAttrValueDao;
import com.atguigu.gulimall.pms.entity.ProductAttrValueEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.PrivateKey;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallPmsApplicationTests {
    @Autowired
    private ProductAttrValueDao productAttrValueDao;
    @Test
    public void contextLoads() {
        QueryWrapper<ProductAttrValueEntity> queryWrapper = new QueryWrapper();
        queryWrapper.eq("spu_id",14);
        queryWrapper.select("DISTINCT  attr_id");
        List<ProductAttrValueEntity> productAttrValueList = productAttrValueDao.selectList(queryWrapper);

        List<Object> objects = productAttrValueDao.selectObjs(queryWrapper);
        System.out.println(objects);

        System.out.println(productAttrValueList);
    }

    @Test
    public void fun1(){

    }
}
