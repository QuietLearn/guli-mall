package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.gulimall.pms.dao.*;
import com.atguigu.gulimall.pms.entity.*;
import com.atguigu.gulimall.pms.service.ItemService;
import com.atguigu.gulimall.pms.service.SkuImagesService;
import com.atguigu.gulimall.pms.service.SkuInfoService;
import com.atguigu.gulimall.pms.service.SpuInfoDescService;
import com.atguigu.gulimall.pms.vo.detail.DetailAttrGroup;
import com.atguigu.gulimall.pms.vo.detail.DetailBaseAttrVo;
import com.atguigu.gulimall.pms.vo.detail.DetailSaleAttrVo;
import com.atguigu.gulimall.pms.vo.resp.SkuItemDetailVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    @Qualifier("mainThreadPool")
    ThreadPoolExecutor mainThreadPool;

    @Autowired
    private SkuImagesDao skuImagesDao;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private ProductAttrValueDao productAttrValueDao;
    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;


    @Override
    public SkuItemDetailVo getDetail(Long skuId) throws ExecutionException, InterruptedException {
        //1、当前sku的基本信息   2s
        //2、sku的所有图片  1s
        //3、sku的所有促销信息   2s
        //4、sku的所有销售属性组合   2s
        //5、spu的所有基本属性  1s
        //6、详情介绍  1s
        //正常敲代码9秒以后
        //1、说到异步--就要说到--->线程--说到线程就要说到-->线程池。【看线程池、异步在这里到底如何使用】
//        【大家伙都要执行，但各自可以互相独立，都查完以后聚合到一起即可】
        //2、缓存；
        //多线程；不是为了提升速度[伪 定义]，提升吞吐量；
        SkuItemDetailVo detailVo = new SkuItemDetailVo();

        //1、当前sku的基本信息   2s
       /* CompletableFuture<Long> skuInfoAssemGetSpuIdFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfo = skuInfoDao.selectOne(new QueryWrapper<SkuInfoEntity>().eq("sku_id", skuId));
            BeanUtils.copyProperties(skuInfo, detailVo);
            return skuInfo.getSpuId();
        });*/
        CompletableFuture<SkuInfoEntity> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfo = skuInfoDao.selectById(skuId);
            return skuInfo;
        },mainThreadPool);

        CompletableFuture<Void> SkuInfoAssemFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            assemSkuinfoToDetailVo(skuInfo, detailVo);
        }, mainThreadPool);


        //2、sku的所有图片  1s
        CompletableFuture<List<SkuImagesEntity>> skuImageListCompletableFuture = CompletableFuture.supplyAsync(() -> {
            List<SkuImagesEntity> skuImagesList = skuImagesDao.selectList(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            return skuImagesList;
        }, mainThreadPool);

        CompletableFuture<Void> skuImageUrlCompletableFuture = skuImageListCompletableFuture.thenAcceptAsync(skuImages -> {
            List<String> imageUrlList = Lists.newArrayList();
            skuImages.forEach(skuImage -> {
                imageUrlList.add(skuImage.getImgUrl());
            });
            detailVo.setImgList(imageUrlList);
        }, mainThreadPool);

        //3、sku的所有促销信息   2s


        //4、sku的所有销售属性组合   2s
        CompletableFuture<Void> saleAttrsCmpletableFuture = CompletableFuture.runAsync(() -> {
            List<SkuSaleAttrValueEntity> skuSaleAttrValueList = skuSaleAttrValueDao.selectList(new QueryWrapper<SkuSaleAttrValueEntity>()
                    .eq("sku_id", skuId));
            List<DetailSaleAttrVo> saleAttrs = Lists.newArrayList();

            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                DetailSaleAttrVo detailSaleAttrVo = new DetailSaleAttrVo();
                detailSaleAttrVo.setAttrId(skuSaleAttrValue.getAttrId());
                detailSaleAttrVo.setAttrName(skuSaleAttrValue.getAttrName());

                detailSaleAttrVo.setAttrValues(new String[]{skuSaleAttrValue.getAttrValue()});
                saleAttrs.add(detailSaleAttrVo);
            });
            detailVo.setSaleAttrs(saleAttrs);
        }, mainThreadPool);


        //5、spu的所有基本属性  1s
        CompletableFuture<Void> detailAttrGroupCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {

            Long spuId = skuInfo.getSpuId();
            Long catalogId = skuInfo.getCatalogId();
            List<AttrGroupEntity> attrGroupList = attrGroupDao.selectList(
                    new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catalogId));
//            detailVo.setAttrGroups();
            List<DetailAttrGroup> detailAttrGroupList = assemDtailAttrGroupList(attrGroupList, spuId);
            detailVo.setAttrGroups(detailAttrGroupList);
        },mainThreadPool);

        //6、详情介绍  1s
        CompletableFuture<Void> spuInfoDescCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescDao.selectById(skuInfo.getSpuId());
            
            detailVo.setDesc(spuInfoDescEntity);
        },mainThreadPool);


        //正常敲代码9秒以后
        //1、异步，线程，线程池。
        //2、缓存；
        //thenAcceptAsync 也要保证这个完成，可能上面那个完成，下面thenAcceptAsync 这个未完成
        CompletableFuture<Void> allOfCompletableFuture = CompletableFuture.allOf(skuInfoCompletableFuture, SkuInfoAssemFuture,
                skuImageListCompletableFuture,
                skuImageUrlCompletableFuture, saleAttrsCmpletableFuture,
                detailAttrGroupCompletableFuture,spuInfoDescCompletableFuture);

        allOfCompletableFuture.get();

        //多线程；提升吞吐量；
        return detailVo;
    }

    private List<DetailAttrGroup> assemDtailAttrGroupList(List<AttrGroupEntity> attrGroupList,Long spuId) {
        List<DetailAttrGroup> detailAttrGroupList= Lists.newArrayList();
        attrGroupList.forEach(attrGroup -> {
            DetailAttrGroup detailAttrGroup = new DetailAttrGroup();
            detailAttrGroup.setGroupId(attrGroup.getAttrGroupId());
            detailAttrGroup.setGroupName(attrGroup.getAttrGroupName());

     
            /*List<AttrEntity> attrList = attrDao.selectAttrByGroupId(1,attrGroup.getAttrGroupId());
            attrList.forEach(attrEntity -> {
                DetailBaseAttrVo detailBaseAttrVo = new DetailBaseAttrVo();
                detailBaseAttrVo.setAttrId(attrEntity.getAttrId());
                detailBaseAttrVo.setAttrName(attrEntity.getAttrName());

                List<ProductAttrValueEntity> productAttrValueList = productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>()
                        .eq("spu_id", spuId).in(""));
            });*/
            List<DetailBaseAttrVo> detailBaseAttrVoList = Lists.newArrayList();
            List<ProductAttrValueEntity> productAttrValueList = productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>()
                    .eq("spu_id", spuId));
            productAttrValueList.forEach(productAttrValue -> {
                DetailBaseAttrVo detailBaseAttrVo = new DetailBaseAttrVo();
                detailBaseAttrVo.setAttrId(productAttrValue.getAttrId());
                detailBaseAttrVo.setAttrName(productAttrValue.getAttrName());
                detailBaseAttrVo.setAttrValues(productAttrValue.getAttrValue().split(","));
                detailBaseAttrVoList.add(detailBaseAttrVo);
            });


            detailAttrGroup.setDetailBaseAttrVoList(detailBaseAttrVoList);
            detailAttrGroupList.add(detailAttrGroup);
        });
        return detailAttrGroupList;
    }

    private void assemSkuinfoToDetailVo(SkuInfoEntity skuInfo, SkuItemDetailVo detailVo) {
        SkuInfoEntity product = detailVo.getProduct();
        product.setSkuId(skuInfo.getSkuId());
        product.setSpuId(skuInfo.getSpuId());
        product.setCatalogId(skuInfo.getCatalogId());
        product.setBrandId(skuInfo.getBrandId());
        product.setSkuTitle(skuInfo.getSkuTitle());
        product.setSkuSubtitle(skuInfo.getSkuSubtitle());
        product.setPrice(skuInfo.getPrice());
        product.setWeight(skuInfo.getWeight());
    }
}
