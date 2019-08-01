package com.atguigu.gulimall.sms.dao;

import com.atguigu.gulimall.sms.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author heyijie
 * @email hyj78586421@outlook.com
 * @date 2019-08-01 19:20:01
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
