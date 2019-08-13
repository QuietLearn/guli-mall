package com.atguigu.gulimall.pms.dao;

import com.atguigu.gulimall.pms.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Attr;

import java.util.List;

/**
 * 商品属性
 * 
 * @author leifengyang
 * @email lfy@atguigu.com
 * @date 2019-08-01 15:52:32
 */
@Mapper
@Repository
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<AttrEntity> selectAttrByGroupId(@Param("attrType") Integer attrType, @Param("groupId") Long groupId);
	
}
