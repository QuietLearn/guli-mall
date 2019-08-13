package com.atguigu.gulimall.pms.service;

import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.pms.vo.req.BaseAttrVo;
import com.atguigu.gulimall.pms.vo.req.SkuInfoVo;
import com.atguigu.gulimall.pms.vo.req.SpuBaseattrSkuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.pms.entity.SpuInfoEntity;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import java.util.List;


/**
 * spu信息
 *
 * @author leifengyang
 * @email lfy@atguigu.com
 * @date 2019-08-01 15:52:32
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    ServerResponse<Long> saveSpuBaseInfo(SpuBaseattrSkuSaveVo spuBaseattrSkuSaveVo);

    void saveSpuInfoImages(Long spuId, String[] spuImages);

    ServerResponse saveSpuBaseAttrs(Long spuId, List<BaseAttrVo> baseAttrs);

    PageVo queryPage(QueryCondition params);

    ServerResponse<PageVo> listSpuInfoByKeywordAndCategoryId(QueryCondition queryCondition, Integer catId);

    /**
     * 保存spu商品信息，包含商品的基本属性信息和sku具体商品信息【包含具体商品销售属性和打折营销信息】
     * @param spuBaseattrSkuSaveVo
     * @return
     */
    ServerResponse saveSpuBaseattrSkuSaveVo(SpuBaseattrSkuSaveVo spuBaseattrSkuSaveVo);

    void saveSkuInfos(Long spuId, List<SkuInfoVo> skus) ;

    ServerResponse updateSpuStatus(Long spuId, Integer status);
}

