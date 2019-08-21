package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.gulimall.commons.bean.*;
import com.atguigu.gulimall.commons.to.SkuSaleInfoTo;
import com.atguigu.gulimall.commons.to.es.EsSkuAttributeValue;
import com.atguigu.gulimall.commons.to.es.EsSkuVo;
import com.atguigu.gulimall.commons.to.wms.SkuStockVo;
import com.atguigu.gulimall.pms.dao.*;
import com.atguigu.gulimall.pms.entity.*;
import com.atguigu.gulimall.pms.feign.EsFeignService;
import com.atguigu.gulimall.pms.feign.SmsSkuMarketService;
import com.atguigu.gulimall.pms.feign.WmsFeignService;
import com.atguigu.gulimall.pms.service.*;
import com.atguigu.gulimall.pms.vo.SpuInfoEntityVo;
import com.atguigu.gulimall.pms.vo.req.BaseAttrVo;
import com.atguigu.gulimall.pms.vo.req.SaleAttrVo;
import com.atguigu.gulimall.pms.vo.req.SkuInfoVo;
import com.atguigu.gulimall.pms.vo.req.SpuBaseattrSkuSaveVo;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

//    @Autowired
//    SmsSkuSaleInfoFeignService smsSkuSaleInfoFeignService;

    @Autowired
    private SmsSkuMarketService skuMarketService;

    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private CategoryService categoryService;


    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    //pms_product_attr_value
    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesDao skuImagesDao;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;


    @Autowired
    SpuInfoDescService spuInfoDescService;

    //pms_product_attr_value
    @Autowired
    ProductAttrValueDao spuAttrValueDao;

    @Autowired
    EsFeignService esFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    BrandDao brandDao;

    @Autowired
    SpuInfoDao spuInfoDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageVo(page);
    }

    /**
     * 按照spuid,spuname,分类id检索商品
     * @param queryCondition
     * @param catId
     * @return
     */
    @Override
    public ServerResponse<PageVo> listSpuInfoByKeywordAndCategoryId(QueryCondition queryCondition, Integer catId) {
        if (StringUtils.isBlank(queryCondition.getKey())&&catId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR);
        }
        //1、封装查询条件
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        if (catId!=null&&catId!=0){//查全站的
            // catalog_id = 227 and (spu_name like ss or id = 1)
            ServerResponse<List<Long>> deepCategoryIdList = categoryService.getDeepCategory(catId);

            queryWrapper.in("catalog_id",deepCategoryIdList.getData());
//            queryWrapper.eq("catalog_id",catId);
        }

        String key = queryCondition.getKey();
        if (StringUtils.isNotBlank(key)){
            queryWrapper.and(obj->{
                obj.like("spu_name",queryCondition.getKey());
                obj.or().like("id",queryCondition.getKey());
                return obj;
            });
        }

        //2、封装翻页条件
        //3、去数据库查询
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(queryCondition),
                queryWrapper
        );

        if (page==null|| CollectionUtils.isEmpty(page.getRecords())){
            return ServerResponse.createByErrorMessage("该查询条件无法查出任何商品信息");
        }

        PageVo pageVo = new PageVo(page);

        pageVo.setList(assemSpuInfoEntityVoList(page.getRecords()));

        return ServerResponse.createBySuccess("查询成功",pageVo);
    }

    /**
     * 这边利用了前端构建整个大对象，而不是用redis分布存储对象，
     * 好处在于前端就可以完成构建操作，且填写到一半的废数据可以在前端可以直接丢弃，不会影响数据的完整性
     *
     * 加了声明式事务以后，执行时会用代理对象的代理方法 所有的数据库操作都会用同一个connection，且开启事务并串起来，等到最后提交时，
     * 在将所有操作提交
     * @param spuBaseattrSkuSaveVo
     * @return
     */

    //
//    设置全都回滚很重要

//    @Transactional(rollbackFor = {Exception.class})
    @GlobalTransactional(rollbackFor = {Exception.class})
    @Override
    public ServerResponse saveSpuBaseattrSkuSaveVo(SpuBaseattrSkuSaveVo spuBaseattrSkuSaveVo) {

      /**
           *  第三次
                *      大保存：
         *          this代理.saveSpuBaseInfo(设置了requires_new)
                *          this代理.saveSpuInfoImages(设置了requires_new);
         *  这个是可以的.....
         *
         *
         *  为啥？
         *      @Transactional：底层是用aop；
         *      事务要生效必须是代理对象在调用；
         *      this不是代理对象，就相当于代码粘到了大方法里面，this.方法（）；是跟外面用的一个事务
                *
         *  解决：
         *      1）、把这些放别人的service；
         *      2）、如果能获取到本类的代理对象，直接调用本类方法就完事;
         *          如何获取：
         *              1）、导入aop的场景依赖；spring-boot-starter-aop
                *              2）、开启aop的高级功能；@EnableAspectJAutoProxy：开启自动代理
                *              3）、同时要暴露代理对象；@EnableAspectJAutoProxy(exposeProxy=true)
         *              4）、获取代理对象；
         *                  SpuInfoService proxy = (SpuInfoService) AopContext.currentProxy();
         */
//        用代理类而非将方法原样执行，没有做方法增强

//        使用代理执行代理方法，在业务方法前后begin开启新事务，设置事务传递机制以不受原来事务影响
        SpuInfoService proxy = (SpuInfoService) AopContext.currentProxy();
        //1、SpuInfo spu基本信息存储
        ServerResponse<Long> response = proxy.saveSpuBaseInfo(spuBaseattrSkuSaveVo);
        Long spuId=null;
        if(response.isSuccess()) {
            spuId = response.getData();
        }

//        String spuDescription = spuBaseattrSkuSaveVo.getSpuDescription();
//       2、spuInfoDesc  spu详细信息 详情图存储
        String[] spuImages = spuBaseattrSkuSaveVo.getSpuImages();
        proxy.saveSpuInfoImages(spuId,spuImages);

//       3、spu 基本属性 存储
        List<BaseAttrVo> baseAttrs = spuBaseattrSkuSaveVo.getBaseAttrs();
        ServerResponse saveSpuBaseAttrsResponse = proxy.saveSpuBaseAttrs(spuId, baseAttrs);
        if(!saveSpuBaseAttrsResponse.isSuccess())
            return saveSpuBaseAttrsResponse;



//      4、sku info 基本信息存储
        List<SkuInfoVo> skus = spuBaseattrSkuSaveVo.getSkus();

        List<SkuInfoEntity> skuInfoEntities = assemSkuInfoList(spuId,spuBaseattrSkuSaveVo.getBrandId(),spuBaseattrSkuSaveVo.getCatalogId(), skus);
        skuInfoEntities.forEach(skuInfoEntity -> {
            boolean isSkuinfoSave = skuInfoService.save(skuInfoEntity);
            //         skuInfoService.saveBatch(skuInfoEntities);
            /*if (!isSkuinfoSave){
                return ServerResponse.createByErrorMessage("批量插入 skuInfo 基本信息失败");
            }*/
        });



//      5、sku images 详情图存储
//      6、sku 销量属性存储
//        可以用for单个单个插入，比较符合逻辑一点，下面我用自己的想法
        List<SkuImagesEntity> skuImagesEntityList;

        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities;

        SkuInfoEntity skuInfoEntity;
        for (int i = 0; i < skus.size(); i++) {
            SkuInfoVo skuInfoVo = skus.get(i);
            String[] images = skuInfoVo.getImages();
            skuInfoEntity = skuInfoEntities.get(i);

            if (images != null && images.length > 0) {
                Long skuId = skuInfoEntity.getSkuId();
                skuImagesEntityList = assemSkuimageList(skuId, images);
//                因为是null所以不会进来
                for (SkuImagesEntity skuImagesEntity : skuImagesEntityList) {
                    skuImagesDao.insert(skuImagesEntity);
                }
//                todo seata目前不支持批量插入，只能这样了
//                skuImagesDao.insertBatch(skuImagesEntityList);
            }
//            6、sku 销量属性获取
            List<SaleAttrVo> saleAttrs = skuInfoVo.getSaleAttrs();
            skuSaleAttrValueEntities = assemSkuSaleAttrValueList(skuInfoEntity, saleAttrs);
            for (SkuSaleAttrValueEntity skuSaleAttrValueEntity : skuSaleAttrValueEntities) {
                skuSaleAttrValueDao.insert(skuSaleAttrValueEntity);
            }
//             todo seata目前不支持批量插入，只能这样了
//            skuSaleAttrValueDao.insertBatch(skuSaleAttrValueEntities);
        }
//        skuInfoEntities

//        7、远程调用sms接口 保存商品营销信息到sms数据库中
        List<SkuSaleInfoTo> skuSaleInfoToList = assemSkuSaleInfoToList(skuInfoEntities, skus);
//      因为是远程接口的缘故，所以把所有数据封装成一个接口传输 ，保证请求减少增加成功率
        skuMarketService.saveSkuSaleInfos(skuSaleInfoToList);

//        int i=1/0;

        return ServerResponse.createBySuccessMessage("保存spu info及其他数据成功");
    }


//    (propagation = Propagation.REQUIRES_NEW)
    @Transactional
    public ServerResponse<Long> saveSpuBaseInfo(SpuBaseattrSkuSaveVo spuBaseattrSkuSaveVo){

        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuBaseattrSkuSaveVo,spuInfoEntity);
        int spuInfoInsertCount = baseMapper.insert(spuInfoEntity);
        /*if (spuInfoInsertCount<=0){
            return ServerResponse.createByErrorMessage("插入spu商品信息失败");
        }*/
        return ServerResponse.createBySuccess(spuInfoEntity.getId());
    }

//    (propagation = Propagation.REQUIRES_NEW)
    @Transactional
    public void saveSpuInfoImages(Long spuId, String[] spuImages){
//        Long spuId = spuInfoEntity.getId();

        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
//        也可以用stringbuilder拼接","
//       stringutils.join的内部也是用stringbuilder，但逻辑更加安全
        spuInfoDescEntity.setDecript(StringUtils.join(spuImages,","));

        spuInfoDescDao.insert(spuInfoDescEntity);
        /* int SpuInfoDescInsertCount =
        if (SpuInfoDescInsertCount<=0){
            return ServerResponse.createByErrorMessage("插入spu商品 详情 信息失败");
        }*/
    }

//    (propagation = Propagation.REQUIRES_NEW)
    @Transactional
    public ServerResponse saveSpuBaseAttrs(Long spuId, List<BaseAttrVo> baseAttrs) {
        List<ProductAttrValueEntity> productAttrValueEntityList = assemProductAttrValueList(spuId,baseAttrs);

        for (ProductAttrValueEntity productAttrValueEntity : productAttrValueEntityList) {
            productAttrValueDao.insert(productAttrValueEntity);
        }
//        int productAttrValueBatchInsertCount = productAttrValueDao.insertBatch(productAttrValueEntityList);
        /*if (productAttrValueBatchInsertCount<=0){
            return ServerResponse.createByErrorMessage("批量插入spu 基本属性 信息失败");
        }*/
        return ServerResponse.createBySuccessMessage("批量插入spu 基本属性 信息成功");
    }



    private List<SkuSaleInfoTo> assemSkuSaleInfoToList(List<SkuInfoEntity> skuInfoEntities, List<SkuInfoVo> skus) {
        List<SkuSaleInfoTo> skuSaleInfoToList = Lists.newArrayList();
        for (int i = 0; i < skus.size(); i++) {
            SkuSaleInfoTo skuSaleInfoTo = new SkuSaleInfoTo();
            BeanUtils.copyProperties(skus.get(i),skuSaleInfoTo);
            skuSaleInfoTo.setSkuId(skuInfoEntities.get(i).getSkuId());
            skuSaleInfoToList.add(skuSaleInfoTo);
        }
        return skuSaleInfoToList;
    }

    private List<SkuSaleAttrValueEntity> assemSkuSaleAttrValueList(SkuInfoEntity skuInfoEntity, List<SaleAttrVo> saleAttrs){
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = Lists.newArrayList();
        saleAttrs.forEach( saleAttr->{
            SkuSaleAttrValueEntity skuSaleAttrValue = new SkuSaleAttrValueEntity();

            AttrEntity attrEntity = attrDao.selectById(saleAttr.getAttrId());

            skuSaleAttrValue.setAttrId(attrEntity.getAttrId());
            skuSaleAttrValue.setAttrName(attrEntity.getAttrName());
//            设置 值
            skuSaleAttrValue.setAttrValue(saleAttr.getAttrValue());

            skuSaleAttrValue.setAttrSort(0);
            skuSaleAttrValue.setSkuId(skuInfoEntity.getSkuId());
            skuSaleAttrValue.setSpuId(skuInfoEntity.getSpuId());


            skuSaleAttrValueEntityList.add(skuSaleAttrValue);
        });

        return skuSaleAttrValueEntityList;
    }

    private List<SkuInfoEntity> assemSkuInfoList(Long spuId,Long brandId,Long catalogId ,List<SkuInfoVo> skus){
        List<SkuInfoEntity> skuInfoList = Lists.newArrayList();

        skus.forEach(skuInfo->{
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();

            BeanUtils.copyProperties(skuInfo,skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
//           5位 大写的随机字符串
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0, 5).toUpperCase());
            skuInfoEntity.setCatalogId(catalogId);
            skuInfoEntity.setBrandId(brandId);
            String[] images = skuInfo.getImages();
            if (images != null && images.length > 0) {
                skuInfoEntity.setSkuDefaultImg(images[0]);
            }

            skuInfoList.add(skuInfoEntity);
        });

        return skuInfoList;
    }

    private List<SkuImagesEntity> assemSkuimageList(Long skuId,String[] images){
        List<SkuImagesEntity> skuImagesEntityList = Lists.newArrayList();
        if (images == null || images.length <= 0)
            return null;

        for (int i = 0; i < images.length; i++) {
            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
            skuImagesEntity.setSkuId(skuId);
            skuImagesEntity.setImgUrl(images[i]);
            skuImagesEntity.setImgSort(0);
            skuImagesEntity.setDefaultImg(i==0?1:0);
            skuImagesEntityList.add(skuImagesEntity);
        }

        return skuImagesEntityList;
    }



    private List<ProductAttrValueEntity> assemProductAttrValueList(Long spuId,List<BaseAttrVo> baseAttrs){
        List<ProductAttrValueEntity> productAttrValueEntityList = Lists.newArrayList();

        baseAttrs.forEach(baseAttr->{
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();

//            不在 表pms_product_attr_value插入销售属性  是因为attrValue销售属性不同sku有多个，
//            那么在属性展示的时候，确定的sku显示唯一销售属性显示多个？不可能的啊
            productAttrValueEntity.setAttrId(baseAttr.getAttrId());
            productAttrValueEntity.setAttrName(baseAttr.getAttrName());
            productAttrValueEntity.setAttrValue(StringUtils.join(baseAttr.getValueSelected(),","));
            productAttrValueEntity.setSpuId(spuId);
            productAttrValueEntity.setAttrSort(0);
            productAttrValueEntity.setQuickShow(0);


            productAttrValueEntityList.add(productAttrValueEntity);
        });

        return productAttrValueEntityList;
    }


    private List<SpuInfoEntityVo> assemSpuInfoEntityVoList(List<SpuInfoEntity> spuInfoEntityList){
        ArrayList<SpuInfoEntityVo> SpuInfoEntityVoList = Lists.newArrayList();

        spuInfoEntityList.forEach(spuInfoEntity->{


            SpuInfoEntityVo spuInfoEntityVo = new SpuInfoEntityVo();
            BeanUtils.copyProperties(spuInfoEntity,spuInfoEntityVo);

            QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper();
            queryWrapper.select("name");
            queryWrapper.eq("cat_id",spuInfoEntity.getCatalogId());
            CategoryEntity categoryEntity = categoryDao.selectOne(queryWrapper);
            spuInfoEntityVo.setCatagoryName(categoryEntity.getName());
            SpuInfoEntityVoList.add(spuInfoEntityVo);
        });


        return SpuInfoEntityVoList;
    }



    /**
     * 传播行为；
     *   大保存：80s
     *   小保存：3s；
     *   但是小保存3s没作用；
     *
     *   大保存{
     *       小保存(rollbackFor = {ArithmeticException.class},timeout = 3)
     *   }
     * @param spuId
     * @param skus
     */
    //保存sku的所有详情
    @Transactional(rollbackFor = {ArithmeticException.class})
    @Override
    public void saveSkuInfos(Long spuId, List<SkuInfoVo> skus) {
        //0、查出这个spu的信息，
        SpuInfoEntity spuInfo = this.getById(spuId);

        List<SkuSaleInfoTo> tos = new ArrayList<SkuSaleInfoTo>();
        //catalog_id  brand_id
        //1、保存sku的info信息
        for (SkuInfoVo skuVo : skus) {
            String[] images = skuVo.getImages();

            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            skuInfoEntity.setBrandId(spuInfo.getBrandId());
            skuInfoEntity.setCatalogId(spuInfo.getCatalogId());
            skuInfoEntity.setPrice(skuVo.getPrice());
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0, 5).toUpperCase());
            if (images != null && images.length > 0) {
                skuInfoEntity.setSkuDefaultImg(skuVo.getImages()[0]);
            }

            skuInfoEntity.setSkuDesc(skuVo.getSkuDesc());
            skuInfoEntity.setSkuName(skuVo.getSkuName());
            skuInfoEntity.setSkuSubtitle(skuVo.getSkuSubtitle());
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setSkuTitle(skuVo.getSkuTitle());
            skuInfoEntity.setWeight(skuVo.getWeight());
            //保存sku的基本信息
            skuInfoDao.insert(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();

            //2、保存sku的所有对应图片
            for (int i = 0; i < images.length; i++) {
                SkuImagesEntity imagesEntity = new SkuImagesEntity();
                imagesEntity.setSkuId(skuId);
                imagesEntity.setDefaultImg(i == 0?1:0);
                imagesEntity.setImgUrl(images[i]);
                imagesEntity.setImgSort(0);
                skuImagesDao.insert(imagesEntity);
            }

            //3、当前sku的所有销售属性组合保存起来
            List<SaleAttrVo> saleAttrs = skuVo.getSaleAttrs();
            for (SaleAttrVo attrVo : saleAttrs) {

                //查询当前属性的信息

                SkuSaleAttrValueEntity entity = new SkuSaleAttrValueEntity();
                entity.setAttrId(attrVo.getAttrId());
                //查出这个属性的真正信息
                AttrEntity attrEntity = attrDao.selectById(attrVo.getAttrId());
                entity.setAttrName(attrEntity.getAttrName());
                entity.setAttrSort(0);
                entity.setAttrValue(attrVo.getAttrValue());
                entity.setSkuId(skuId);
                //sku与销售属性的关联关系
                skuSaleAttrValueDao.insert(entity);

            }

            //以上都是pms系统完成的工作


            //以下需要由sms完成，保存每一个sku的相关优惠数据
            SkuSaleInfoTo info = new SkuSaleInfoTo();
            BeanUtils.copyProperties(skuVo,info);
            info.setSkuId(skuId);

            tos.add(info);

        }

//        int i = 10/0;
        //2、发给sms，让他去处理。我们不管
        log.info("pms准备给sms发出数据...{}",tos);
        skuMarketService.saveSkuSaleInfos(tos);
        log.info("pms给sms发出数据完成...");


    }

    /**
     * 商品上下架
     *
     * @param spuId
     * @param status
     */
    @Override
    public ServerResponse updateSpuStatus(Long spuId, Integer status) {
        if (status==1){
            //上架
            return spuUp(spuId, status);
        } else{
            return spuDown(spuId, status);
        }
    }

    private ServerResponse spuUp(Long spuId, Integer status) {

        //因为spu 决定 sku，所以sku的 品牌信息和分类信息全都一致，只查询spu对应的这两个信息即可
        //1、查出我们接下来要使用的基本信息；
        SpuInfoEntity spuInfo = spuInfoDao.selectById(spuId);

        QueryWrapper<BrandEntity> brandQueryWrapper = new QueryWrapper<BrandEntity>().eq("brand_id", spuInfo.getBrandId());
        brandQueryWrapper.select("name","logo");
        BrandEntity brandEntity = brandDao.selectOne(brandQueryWrapper);
        String brandName = brandEntity.getName();
        String brandLogo =  brandEntity.getLogo();


        QueryWrapper<CategoryEntity> categoryEntityQueryWrapper = new QueryWrapper<CategoryEntity>().eq("cat_id", spuInfo.getCatalogId());
        categoryEntityQueryWrapper.select("name");
        String categoryName = (String) categoryDao.selectObjs(categoryEntityQueryWrapper).get(0);



        //2、上架：将商品需要检索的信息放在es中、下架：将商品需要检索的信息从es中删除；
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper();
        queryWrapper.eq("spu_id",spuId);
        List<SkuInfoEntity> skuInfoList = skuInfoDao.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(skuInfoList)) {
            return ServerResponse.createByErrorMessage("不用上架了，目前spu 没有任何sku商品 可以上架，请添加");
        }

        //1.1)查出这个spu对应的额sku的所有库存信息；
        List<Long> skuIds = new ArrayList<>();
        skuInfoList.forEach(skuInfo-> {
            skuIds.add(skuInfo.getSkuId());
        });

        ServerResponse<List<SkuStockVo>> skuStockResponse = wmsFeignService.skuWareInfos(skuIds);
        List<SkuStockVo> skuStockInfoList = null;
        if (skuStockResponse.isSuccess()){
            skuStockInfoList = skuStockResponse.getData();
        }

        ArrayList<EsSkuVo> esSkuVoList = Lists.newArrayList();

        List<EsSkuAttributeValue> esSkuAttributeValueList = assemSkuRetrievedBaseAttrValueList(spuId);

        //2、构造所有需要保存在es中的sku信息
        for (SkuInfoEntity skuInfo : skuInfoList) {
            EsSkuVo esSkuVo = new EsSkuVo();
            BeanUtils.copyProperties(skuInfo,esSkuVo);

            esSkuVo.setProductCategoryId(spuInfo.getCatalogId());

            esSkuVo.setBrandName(brandName);
            esSkuVo.setBrandLogo(brandLogo);

            esSkuVo.setProductCategoryName(categoryName);

            skuStockInfoList.forEach(skuStockInfo->{
                if (skuStockInfo.getSkuId()==esSkuVo.getSkuId()){
                    esSkuVo.setStock(skuStockInfo.getStock());
                }
            });

            //老师因为疏忽数据库表没有设计销量字段
            esSkuVo.setSale(0);

            //综合排序，热度评分目前先设置为0
            esSkuVo.setSort(0);
//          //封装sku_id，用于批量查询库存，不然每个sku_id都单独分布调用远程服务一次，
//          1来耗费整个系统性能，二来容易因为网络原因出错，得不到返回数据
            esSkuVo.setAttrValueList(esSkuAttributeValueList);
            esSkuVoList.add(esSkuVo);
        }

        //3、远程调用search服务，将商品上架；
        ServerResponse response = esFeignService.spuUp(esSkuVoList);
        if (response.isSuccess()){
            //远程调用成功
            //本地修改数据库;
            SpuInfoEntity updateSpuInfo = new SpuInfoEntity();
            updateSpuInfo.setId(spuId);
            updateSpuInfo.setPublishStatus(status);
//            updateSpuInfo.setGmtModified(new Date()); 有metaHandler
//            这边注意entity的类型全部用包装类型，因为有null’值可以选择，mp判断为空的不会进行更新操作
            //按照id更新其他设置了的字段
            spuInfoDao.updateById(updateSpuInfo);

        }


        return null;
    }

    private ServerResponse spuDown(Long spuId, Integer status) {
        return null;
    }

    /**
     * 目前只封装sku 也即是 spu的可检索基本属性
     * @param spuId
     * @return
     */
    private List<EsSkuAttributeValue> assemSkuRetrievedBaseAttrValueList(Long spuId){
        QueryWrapper<ProductAttrValueEntity> queryWrapper = new QueryWrapper();
        queryWrapper.eq("spu_id",spuId);
//        queryWrapper.select("DISTINCT  attr_id");
//
        List<Long> attrIdList = Lists.newLinkedList();
        List<ProductAttrValueEntity> productAttrValueList = productAttrValueDao.selectList(queryWrapper);
        productAttrValueList.forEach(productAttrValueEntity -> {
            Long attrId = productAttrValueEntity.getAttrId();
            attrIdList.add(attrId);
        });

//        最好用枚举吗？
        QueryWrapper<AttrEntity> attrQueryWrapper = new QueryWrapper();
        attrQueryWrapper.eq("search_type",1);
        attrQueryWrapper.in("attr_id",attrIdList);
        queryWrapper.select("DISTINCT  attr_id");

        List<Object> retrievedAttrIdList = attrDao.selectObjs(attrQueryWrapper);
        /*for (Object object : retrievedAttrIdList) {
            Long retrievedAttrId =(Long)object;

        }*/

        List<EsSkuAttributeValue> esSkuAttributeValueList = Lists.newArrayList();
        for (ProductAttrValueEntity productAttrValueEntity : productAttrValueList) {
            for (Object retrievedAttrIdObj : retrievedAttrIdList) {
                Long retrievedAttrId = (Long) retrievedAttrIdObj;
                if (productAttrValueEntity.getAttrId()==retrievedAttrId){
                    EsSkuAttributeValue esSkuAttributeValue = new EsSkuAttributeValue();
                    BeanUtils.copyProperties(productAttrValueEntity,esSkuAttributeValue);
                    esSkuAttributeValue.setProductBaseAttrValueId(productAttrValueEntity.getId());
//                    esSkuAttributeValue.setSpuId();

                    esSkuAttributeValueList.add(esSkuAttributeValue);
                }

            }
        }



        return  esSkuAttributeValueList;

    }

}