package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.pms.entity.AttrEntity;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.Query;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import com.atguigu.gulimall.pms.dao.SkuInfoDao;
import com.atguigu.gulimall.pms.entity.SkuInfoEntity;
import com.atguigu.gulimall.pms.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public ServerResponse listAllSkuInfoInSpu(Long spuId) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper();
        queryWrapper.eq("spu_id",spuId);
        List<SkuInfoEntity> skuInfoEntities = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(skuInfoEntities)){
            return ServerResponse.createByErrorMessage("该spu下没有任何sku信息");
        }

        return ServerResponse.createBySuccess("查询"+spuId+"spu 下sku信息成功",skuInfoEntities);
    }

}