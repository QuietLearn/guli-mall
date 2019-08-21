package com.atguigu.gulimall.search.vo;

import com.atguigu.gulimall.commons.to.es.EsSkuVo;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponse implements Serializable {
//    private SearchResponseAttrVo brand;//品牌,vo里面的value值是当前品牌的信息json对象，如名字，图片
    private List<BrandVo> brand;
//    private SearchResponseAttrVo catelog;//分类
    //所有商品的顶头显示的筛选属性
    private List<CategoryVo> catelog;
    private List<SearchResponseAttrVo> attrs = new ArrayList<>();

    //检索出来的商品信息
//    todo 可以让对象的属性少一點，因为只是展示，只有展示的属性即可，其他用来排序的属性是存在es里的
    private List<EsSkuVo> products = new ArrayList<>();

    private Long total;//总记录数
    private Integer pageSize;//每页显示的内容
    private Integer pageNum;//当前页面


}
