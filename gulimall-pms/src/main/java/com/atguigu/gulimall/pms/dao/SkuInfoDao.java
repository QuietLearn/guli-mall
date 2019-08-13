package com.atguigu.gulimall.pms.dao;

import com.atguigu.gulimall.pms.entity.SkuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * sku信息
 * 
 * @author leifengyang
 * @email lfy@atguigu.com
 * @date 2019-08-01 15:52:32
 */
@Mapper
@Repository
public interface SkuInfoDao extends BaseMapper<SkuInfoEntity> {
	
}
