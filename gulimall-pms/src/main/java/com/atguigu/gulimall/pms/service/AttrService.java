package com.atguigu.gulimall.pms.service;

import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.pms.vo.req.AttrGidVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.pms.entity.AttrEntity;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.QueryCondition;


/**
 * 商品属性
 *
 * @author leifengyang
 * @email lfy@atguigu.com
 * @date 2019-08-01 15:52:32
 */
public interface AttrService extends IService<AttrEntity> {

    PageVo queryPage(QueryCondition params);

    /**
     * 查询某个三级分类下的所有属性
     * @param categoryId
     * @return
     */
    ServerResponse selectAttrByCategoryId(QueryCondition queryCondition,Long categoryId,int attrType);

    /**
     * 查询某个分组下对应的所有属性
     * @param groupId
     * @return
     */
    ServerResponse selectAttrByGroupId(QueryCondition queryCondition, Long groupId,Integer attrType);

    ServerResponse selectAttrInfoWithGroup(Long attrId);

    ServerResponse<Object> saveAttr(AttrGidVo attr);
}

