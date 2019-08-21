package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class BrandVo implements Serializable {
    private Long brandId;//1  品牌
    //当前属性值的所有值
    //JSON字符串 {catelogId:1,name:华为,logo:xxx},{id:2,name:小米} brand或者分类的数组直接以json形式存储为String
    private String brandName;
    //属性名称
    private String logo;
//    private String name;//网络制式，分类   品牌

}
