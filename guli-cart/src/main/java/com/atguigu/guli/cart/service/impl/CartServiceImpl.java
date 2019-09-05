package com.atguigu.guli.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.guli.cart.exception.SaveCartItemToRedisCartException;
import com.atguigu.guli.cart.feign.SkuCouponRedutionFeignService;
import com.atguigu.guli.cart.feign.SkuFeignService;
import com.atguigu.guli.cart.service.CartService;
import com.atguigu.guli.cart.to.SkuCouponTo;
import com.atguigu.guli.cart.vo.CartItemVo;
import com.atguigu.guli.cart.vo.CartVo;
import com.atguigu.guli.cart.vo.SkuCouponVo;
import com.atguigu.guli.cart.vo.SkuFullReductionVo;
import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.commons.to.pms.SkuInfoVo;
import com.atguigu.gulimall.commons.utils.GuliJwtUtils;
import com.atguigu.gulimall.commons.utils.JsonUtil;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private SkuFeignService skuFeignService;

    @Autowired
    SkuCouponRedutionFeignService skuCouponRedutionFeignService;

    @Autowired
    @Qualifier("mainExecutor")
    ThreadPoolExecutor executor;

    /**
     * 获取购物车
     * @param userKey
     * @param authorization
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public ServerResponse getCartDetail(String userKey, String authorization) throws ExecutionException, InterruptedException {
        String loginUserCartPrefixKey;

        //1、获取userkey的前缀
        String tempCartPrefixKey = buildTempCartKey(userKey);

        String generateUserKey = tempCartPrefixKey.substring(Constant.CartInfo.CART_PREFIX.length());

        if (!StringUtils.isBlank(authorization)) {

            Map<String, Object> jwtBody = GuliJwtUtils.getJwtBody(authorization);
            Integer memberId = (Integer) jwtBody.get("memberId");

            loginUserCartPrefixKey = Constant.CartInfo.CART_PREFIX + memberId;

            //合并
            // 先将临时购物车的数据合并到用户购物车中;
            mergeCart(tempCartPrefixKey,loginUserCartPrefixKey);

            return getCartVoFromRedis(generateUserKey,loginUserCartPrefixKey);
        }else {
            //add 2 临时
            return getCartVoFromRedis(generateUserKey,tempCartPrefixKey);
        }


    }

    /**
     * 封装 获取redis的购物车item 数据
     * @param userkey
     * @param redisHashKey
     * @return
     */
    private ServerResponse<CartVo> getCartVoFromRedis(String userkey,String redisHashKey){


        RMap<String, String> cartItemMap = redissonClient.getMap(redisHashKey);
        Collection<String> cartItemValues = cartItemMap.values();
        CartVo cartVo = new CartVo();
        cartVo.setUserKey(userkey);
        if (CollectionUtils.isEmpty(cartItemValues)){
            return ServerResponse.createBySuccess("购物车现在是空的，请去添加商品吧",cartVo);
        }

        ArrayList<CartItemVo> cartItemVoList = Lists.newArrayList();
        cartItemValues.forEach(cartItemStr -> {
            CartItemVo cartItemVo = JsonUtil.Json2Obj(cartItemStr, CartItemVo.class);
            cartItemVoList.add(cartItemVo);
        });
        cartVo.setItems(cartItemVoList);


        return  ServerResponse.createBySuccess("获取购物车数据成功",cartVo);
    }


    /**
     * 添加商品到购物车
     * @param skuId
     * @param num
     * @param userKey
     * @param authorization
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    //不需要合并，添加到登录用户的购物车即可， 获取购物车详情，直到用户看到了再显示合并信息再不迟，用户看到的才需要真实信息，^_^
    @Override
    public CartVo addProductToCart(Long skuId, Integer num, String userKey, String authorization) throws ExecutionException, InterruptedException {

        String loginUserCartPrefixKey;

        //先这样吧，实际公司还是key好，不要带前缀
        String tempCartPrefixKey = buildTempCartKey(userKey);

        String generateUserKey = tempCartPrefixKey.substring(Constant.CartInfo.CART_PREFIX.length());


        CartItemVo cartItemVo;
        if (!StringUtils.isBlank(authorization)) {
            Map<String, Object> jwtBody = GuliJwtUtils.getJwtBody(authorization);
            Integer memberId = (Integer) jwtBody.get("memberId");
//            Long.valueOf(jwtBody.get("memberId").toString());

            loginUserCartPrefixKey = Constant.CartInfo.CART_PREFIX + memberId;
            //合并
            // 先将临时购物车的数据合并到用户购物车中;
            mergeCart(tempCartPrefixKey,loginUserCartPrefixKey);

            cartItemVo = saveCartItemToRedisCart(loginUserCartPrefixKey, skuId, num);


        } else {
            //add 2 临时
            cartItemVo = saveCartItemToRedisCart(tempCartPrefixKey,skuId,num);
        }

        //登录购物车 添加商品不能返回 用户的id作为key，因为前端如果以为是临时key，以后带上作为临时购物车，那me会覆盖用户购物车的列表

        redisTemplate.expire(Constant.CartInfo.CART_PREFIX + generateUserKey, Constant.CartInfo.UNLOGIN_CART_TIMEOUT, TimeUnit.DAYS);

        //获取购物车最新的所有购物项
        CartVo cartVo = new CartVo();
        cartVo.setItems(Arrays.asList(cartItemVo));
        cartVo.setUserKey(generateUserKey);
//        map.put("userkey",generateUserKey);

        return cartVo;
    }

    /**
     * 更新购物车数量
     * @param skuId
     * @param num
     * @param userKey
     * @param authorization
     * @return
     */
    @Override
    public CartVo updateCart(Long skuId, Integer num, String userKey, String authorization) {
        Map<String,String> map = getCartKey(userKey, authorization);

        RMap<String, String> cart = redissonClient.getMap(map.get("cartKey"));
        String itemJson = cart.get(skuId.toString());
        CartItemVo cartItemVo = JsonUtil.Json2Obj(itemJson, CartItemVo.class);

        cartItemVo.setNum(num);
        //修改购物车，覆盖redis数据；
        cart.put(skuId.toString(), JsonUtil.obj2Json(cartItemVo));

        //获取 最新 购物车
        CartVo cartVo = getCartVoFromRedis(map.get("userKey"), map.get("cartKey")).getData();

        return cartVo;
    }

    /**
     * 选中/ 不选中购物车
     * @param skuId
     * @param status
     * @param userKey
     * @param authorization
     * @return
     */
    @Override
    public CartVo checkCart(Long[] skuId, Integer status, String userKey, String authorization) {
        Map<String,String> map = getCartKey(userKey, authorization);
        RMap<String, String> cart = redissonClient.getMap(map.get("cartKey"));

        if (skuId != null && skuId.length > 0) {
            for (Long sku : skuId) {
                String json = cart.get(sku.toString());
                CartItemVo itemVo = JSON.parseObject(json, CartItemVo.class);
                itemVo.setCheck(status == 0 ? false : true);
                //更新购物车
                cart.put(sku.toString(), JSON.toJSONString(itemVo));
            }
        }

        //获取到这个购物车
        CartVo cartVo = getCartVoFromRedis(map.get("userKey"), map.get("cartKey")).getData();

        return cartVo;
    }


    /**
     * 有前缀的
     * @return
     */
    private Map<String,String> getCartKey(String userKey,String authorization){
        Map<String,String> map =  new HashMap<>();

        String loginUserCartPrefixKey;
        //1、获取userkey的前缀
        String tempCartPrefixKey = buildTempCartKey(userKey);

        String generateUserKey = tempCartPrefixKey.substring(Constant.CartInfo.CART_PREFIX.length());

        String cartKey;
        if (!StringUtils.isBlank(authorization)) {

            Map<String, Object> jwtBody = GuliJwtUtils.getJwtBody(authorization);
            Integer memberId = (Integer) jwtBody.get("memberId");

            loginUserCartPrefixKey = Constant.CartInfo.CART_PREFIX + memberId;

            cartKey = loginUserCartPrefixKey;

        }else {
            //add 2 临时
            cartKey= tempCartPrefixKey;
        }

        map.put("cartKey",cartKey);
        map.put("userKey",generateUserKey);

        return map;
    }

    private String buildTempCartKey(String userKey) {
        String tempCartPrefixKey;
        if (!StringUtils.isBlank(userKey)){
            tempCartPrefixKey = Constant.CartInfo.CART_PREFIX + userKey;
        } else {
            tempCartPrefixKey = Constant.CartInfo.CART_PREFIX + UUID.randomUUID().toString().replace("-", "");

        }
        return tempCartPrefixKey;
    }


    private CartItemVo saveCartItemToRedisCart(String hashPrefixKey,Long skuId,Integer num) throws ExecutionException, InterruptedException {
        RMap<String, Object> map = redissonClient.getMap(hashPrefixKey);
        String skuStr = (String) map.get(skuId.toString());
        CartItemVo cartItemVo ;

        //2、添加购物车之前先确定购物车中有没有这个商品，如果有就数量+1 如果没有新增
        if (StringUtils.isBlank(skuStr)){
            //从远程查看skuId对应的sku相关信息，并封装到cartItem中，存储到redis
            //todo 从远程查看skuId对应的sku相关信息
            //能给线程池提交任务，但是completebleFuture更加强大，任务的异常都能感知

            //1）、封装基本信息
            //if 里面的是final 的？？
            CartItemVo tempCartItemVo = new CartItemVo();

            CompletableFuture<Void> infoAsync = CompletableFuture.runAsync(() -> {
                //1、查询sku当前商品的详情；
                ServerResponse<SkuInfoVo> skuInfoResponse = skuFeignService.getSKuInfoForCart(skuId);
                if (!skuInfoResponse.isSuccess()) {
                    throw new SaveCartItemToRedisCartException("添加商品到购物车失败，查询商品信息失败");
                }
                SkuInfoVo skuInfoVo = skuInfoResponse.getData();
                //2、购物项
                BeanUtils.copyProperties(skuInfoVo,tempCartItemVo);
                tempCartItemVo.setNum(num);

            }, executor);

            infoAsync.whenCompleteAsync((v,e)->{
                // todo 怎么让它异常直接整个异步编排退出
                log.error("action行为是",v);
                log.error("异步编排出错",e);
            });

            //2）、封装了优惠券信息
            CompletableFuture<Void> couponAsync = CompletableFuture.runAsync(() -> {

                //3、获取当前购物项的优惠券相关信息  //itemVo.setCoupons();
                ServerResponse<List<SkuCouponTo>> coupons = skuCouponRedutionFeignService.getCoupons(skuId);

                //To封装别人传来的数据
                List<SkuCouponTo> data = coupons.getData();


                //vo提取别人传来的数据里面有用的数据
                List<SkuCouponVo> vos = new ArrayList<>();
                if (data != null && data.size() > 0) {
                    for (SkuCouponTo datum : data) {
                        SkuCouponVo couponVo = new SkuCouponVo();
                        BeanUtils.copyProperties(datum, couponVo);
                        vos.add(couponVo);
                    }
                }
                tempCartItemVo.setCoupons(vos);
            }, executor);


            //3、封装商品的满减信息
            CompletableFuture<Void> reductionAsync = CompletableFuture.runAsync(() -> {
                ServerResponse<List<SkuFullReductionVo>> redutions = skuCouponRedutionFeignService.getRedutions(skuId);
                List<SkuFullReductionVo> data = redutions.getData();
                if (data != null && data.size() > 0) {
                    tempCartItemVo.setReductions(data);
                }
            }, executor);

            CompletableFuture.allOf(infoAsync,couponAsync,reductionAsync).get();
            //4、保存购物车数据

            map.put(skuId.toString(),JsonUtil.obj2Json(tempCartItemVo));
            cartItemVo = tempCartItemVo;
        } else {
            cartItemVo = JsonUtil.Json2Obj(skuStr, CartItemVo.class);
            cartItemVo.setNum(cartItemVo.getNum()+num);
            map.put(skuId.toString(),JsonUtil.obj2Json(cartItemVo));

        }
        //

        return cartItemVo;
    }


    /**
     * 合并临时购物车到 用户购物车，反正如果登录不携带userKey还是会给你一个，
     * 老师的是，直接登录查看不会给userKey， 如果第一次临时查看，什么都没有，userKey也没有，那肯定直接返回空， 再给你一个userKey
     *              添加功能 不会给你userKey的
     * @param
     * @return
     */
    private void mergeCart(String tempCartKey,String loginUserCartKey) throws ExecutionException, InterruptedException {
//        redisTemplate.opsForHash().delete();
        RMap<String, String> tempCartItemsOfCartMap = redissonClient.getMap(tempCartKey);
        Collection<String> values = tempCartItemsOfCartMap.values();
        if (!CollectionUtils.isEmpty(values)){
            for (String value : values) {
                CartItemVo cartItemVo = JsonUtil.Json2Obj(value, CartItemVo.class);
                //将临时购物车里面的这个购物项添加到在线购物车里面
                saveCartItemToRedisCart(loginUserCartKey, cartItemVo.getSkuId(), cartItemVo.getNum());
            }
        }
        //合并以后删除临时购物车
        redisTemplate.delete(tempCartKey);
    }

    public static void main(String[] args) {
    }
}

@Data
class CartKey {
    private String key;
    private boolean login;
    private boolean temp;
    private boolean merge;


}