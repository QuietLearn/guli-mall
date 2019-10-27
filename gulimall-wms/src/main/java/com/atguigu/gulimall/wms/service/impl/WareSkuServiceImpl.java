package com.atguigu.gulimall.wms.service.impl;

import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.wms.vo.LockStockVo;
import com.atguigu.gulimall.wms.vo.SkuLock;
import com.atguigu.gulimall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.Query;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import com.atguigu.gulimall.wms.dao.WareSkuDao;
import com.atguigu.gulimall.wms.entity.WareSkuEntity;
import com.atguigu.gulimall.wms.service.WareSkuService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    RedissonClient redisson;

    @Autowired
    Executor executor;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PlatformTransactionManager tm;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    //foreach虽然是异步的，但是无法编排，就是说无法等待它的执行结果才能执行后面的代码
    @Override
    //@Transactional 加了也没有用；1. 异步的； 2. sql语句不会报错，异步执行的某一个sku顶多判断不大于，返回0而已
    //**必须标在public方法上**
    public LockStockVo lockAndCheckStock(List<SkuLockVo> skuLockVoList) throws ExecutionException, InterruptedException {
        LockStockVo stockVo = new LockStockVo();

        //原子引用
        AtomicReference<Boolean> flag = new AtomicReference<>(true);
        List<SkuLock> skuLocks = new ArrayList<>();
        CompletableFuture<Void>[] futures = new CompletableFuture[skuLockVoList.size()];
        //ForkJoinPool

        //1、核心；
        //1）、分布式锁； stock:locked:1   stock:locked:100    stock:locked:110
        //2）、数据库乐观锁机制；update wms_ware_sku set stock_locked= stock_locked+5,version=version+1 where sku_id=1 and version=xx

        //锁的粒度: 粒度越细，并发越高；
        //redisson.getLock("locked:stock");
        String orderToken = "";
        if (skuLockVoList != null && skuLockVoList.size() > 0) {
            orderToken = skuLockVoList.get(0).getOrderToken();
            int i = 0;
            for (SkuLockVo skuLockVo : skuLockVoList) {
                String finalOrderToken = orderToken;
                CompletableFuture<Void> async = CompletableFuture.runAsync(() -> {
                    try {
                        System.out.println("锁库存开始..."+skuLockVo);
                        SkuLock skuLock = lockSku(skuLockVo);
                        System.out.println("锁库存结束..."+skuLockVo);
                        skuLock.setOrderToken(finalOrderToken);
                        skuLocks.add(skuLock);
                        if(skuLock.getSuccess() == false){
                            flag.set(false);
                        };
                    }catch (Exception e){
                    }
                },executor);
                futures[i]=async;
                i++;
            }
        }

        /**
         * 一旦库存锁不住大家都要回滚；
         * 1）、异步情况下；事务在线程级别就隔离了；每一个线程都会单独请求，自己拉一个connection/mybatis的session进行操作，回滚是隔离的
         * 2）、锁库存失败不是异常；而且每一个商品都要单独看是否自己能锁住库存；手动判断回滚条件
         * 3）、一个商品锁不住库存，就提示该商品库存不足重新；
         *          一个没锁住，就都没锁住
         * 4）、不能用最终一致性，不能后台启动线程慢慢回滚，因为太慢了，直接回滚
         *   这时有新的用户请求进来，数据不一致；必须是强一致。【有库存被锁定还没回滚，错误提示没有商品库存】
         *
         * 难点：
         *   多线程事务回滚；
         *
         */



//        CompletableFuture[] objects = (CompletableFuture[]) futures.toArray();
        CompletableFuture.allOf(futures).get();
        stockVo.setLocks(skuLocks);
        stockVo.setLocked(flag.get());

        if(flag.get()){
            //都锁住了....
            //将所有锁住的库存发给消息队列；40mins过期
            rabbitTemplate.convertAndSend("skuStockCreateExchange","dead.skuStock",skuLocks);
        }


        return stockVo;
    }


    /**
     * 锁库存
     *
     * @param skuLockVo
     * @return
     */
    public SkuLock lockSku(SkuLockVo skuLockVo) throws InterruptedException {
        SkuLock skuLock = new SkuLock();

        //1、检查总库存够不够；
        /**
         *     private Long skuId;
         *     private Integer num;
         */
//        Long count = wareSkuDao.checkStock(skuId);
        //分布式锁
        //数据库是共用的，还是仓库服务专属的，能算分布式锁吗【可以，如果有多台实例的话】
        RLock lock = redisson.getLock(Constant.STOCK_LOCKED + skuLockVo.getSkuId());

        boolean b = lock.tryLock(1, 1, TimeUnit.MINUTES);
        //todo 不进行仓库总库存的判断？这不是单个单个进行判断吗，京东都不会这样，太麻烦
        try{
            if(b){
                List<WareSkuEntity> wareSkuEntities = wareSkuDao.getAllWareCanLocked(skuLockVo);
                if (wareSkuEntities != null && wareSkuEntities.size() > 0) {
                    //1、拿到第一个仓库锁库存
                    WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
                    long i = wareSkuDao.lockSku(skuLockVo, wareSkuEntity.getWareId());
                    if (i > 0) {
                        skuLock.setSkuId(skuLockVo.getSkuId());
                        skuLock.setLocked(skuLockVo.getNum());
                        skuLock.setSuccess(true);
                        skuLock.setWareId(wareSkuEntity.getWareId());
                    }

                } else {
                    skuLock.setSkuId(skuLockVo.getSkuId());
                    skuLock.setLocked(0);
                    skuLock.setSuccess(false);
                }
            }else {
            }
        }finally {
            if(lock.isLocked()){
                lock.unlock();
            }
        }

        return skuLock;
    }
}