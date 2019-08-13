package com.atguigu.gulimall.pms.service;

import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.pms.entity.CategoryEntity;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import java.util.List;


/**
 * 商品三级分类
 *
 * @author leifengyang
 * @email lfy@atguigu.com
 * @date 2019-08-01 15:52:32
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageVo queryPage(QueryCondition params);

    /**
     * 展示商品三级分类 树形列表
     * @return
     */
    ServerResponse<List<CategoryEntity>> listCategoryTree(Integer level);

    ServerResponse<List<CategoryEntity>> getChildCategory(Integer catId);

    ServerResponse<List<Long>> getDeepCategory(Integer catId);
}

