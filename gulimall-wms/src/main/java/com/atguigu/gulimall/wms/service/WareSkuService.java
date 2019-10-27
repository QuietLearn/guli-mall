package com.atguigu.gulimall.wms.service;

import com.atguigu.gulimall.wms.vo.LockStockVo;
import com.atguigu.gulimall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.wms.entity.WareSkuEntity;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * 商品库存
 *
 * @author leifengyang
 * @email hyj78586421@outlook.com
 * @date 2019-08-01 18:46:30
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageVo queryPage(QueryCondition params);

    LockStockVo lockAndCheckStock(List<SkuLockVo> skuIds)  throws ExecutionException, InterruptedException;
}

