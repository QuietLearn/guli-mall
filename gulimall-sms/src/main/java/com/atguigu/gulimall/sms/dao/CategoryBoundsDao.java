package com.atguigu.gulimall.sms.dao;

import com.atguigu.gulimall.sms.entity.CategoryBoundsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀/营销数据库---商品分类积分设置
 * 
 * @author heyijie
 * @email hyj78586421@outlook.com
 * @date 2019-08-01 19:20:01
 */
@Mapper
public interface CategoryBoundsDao extends BaseMapper<CategoryBoundsEntity> {
	
}
