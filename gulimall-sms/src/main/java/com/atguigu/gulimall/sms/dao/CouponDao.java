package com.atguigu.gulimall.sms.dao;

import com.atguigu.gulimall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 优惠券信息
 * 
 * @author heyijie
 * @email hyj78586421@outlook.com
 * @date 2019-08-05 17:09:58
 */
@Mapper
@Repository
public interface CouponDao extends BaseMapper<CouponEntity> {
    List<CouponEntity> selectCouponsBySpuId(Long spuId);
}
