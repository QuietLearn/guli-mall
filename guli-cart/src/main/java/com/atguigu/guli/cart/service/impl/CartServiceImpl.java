package com.atguigu.guli.cart.service.impl;

import com.atguigu.guli.cart.exception.SaveCartItemToRedisCartException;
import com.atguigu.guli.cart.feign.SkuFeignService;
import com.atguigu.guli.cart.service.CartService;
import com.atguigu.guli.cart.vo.CartItemVo;
import com.atguigu.guli.cart.vo.CartVo;
import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.commons.to.pms.SkuInfoVo;
import com.atguigu.gulimall.commons.utils.GuliJwtUtils;
import com.atguigu.gulimall.commons.utils.JsonUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private SkuFeignService skuFeignService;

    @Override
    public ServerResponse getCartDetail(String userKey, String authorization) {
        String loginUserCartPrefixKey;

        //1、获取userkey的前缀
        String tempCartPrefixKey = buildTempCartKey(userKey);

        String generateUserKey = tempCartPrefixKey.substring(Constant.CartInfo.CART_PREFIX.length());

        if (!StringUtils.isBlank(authorization)) {

            Map<String, Object> jwtBody = GuliJwtUtils.getJwtBody(authorization);
            Integer memberId = (Integer) jwtBody.get("memberId");

            loginUserCartPrefixKey = Constant.CartInfo.CART_PREFIX + memberId;

            if (!checkTempCartEmpty(tempCartPrefixKey)){
                //合并
                // 先将临时购物车的数据合并到用户购物车中;
            }

            return getCartVoFromRedis(generateUserKey,loginUserCartPrefixKey);
        }else {
            //add 2 临时
            return getCartVoFromRedis(generateUserKey,tempCartPrefixKey);
        }




    }

    public ServerResponse getCartVoFromRedis(String userkey,String redisHashKey){


        RMap<Object, Object> cartItemMap = redissonClient.getMap(redisHashKey);
        Collection<Object> cartItemValues = cartItemMap.values();
        CartVo cartVo = new CartVo();
        cartVo.setUserKey(userkey);
        if (CollectionUtils.isEmpty(cartItemValues)){
            return ServerResponse.createBySuccess("购物车现在是空的，请去添加商品吧",cartVo);
        }

        ArrayList<CartItemVo> cartItemVoList = Lists.newArrayList();
        cartItemValues.forEach(cartItemObj -> {
            String cartItemStr = (String) cartItemObj;
            CartItemVo cartItemVo = JsonUtil.Json2Obj(cartItemStr, CartItemVo.class);
            cartItemVoList.add(cartItemVo);
        });
        cartVo.setItems(cartItemVoList);


        return  ServerResponse.createBySuccess("获取购物车数据成功",cartVo);
    }


    //不需要合并，添加到登录用户的购物车即可， 获取购物车详情，直到用户看到了再显示合并信息再不迟，用户看到的才需要真实信息，^_^
    @Override
    public Map addProductToCart(Long skuId, Integer num, String userKey, String authorization) {

        String loginUserCartPrefixKey;

        String tempCartPrefixKey = buildTempCartKey(userKey);


        if (!StringUtils.isBlank(authorization)) {
            Map<String, Object> jwtBody = GuliJwtUtils.getJwtBody(authorization);
            Integer memberId = (Integer) jwtBody.get("memberId");

            loginUserCartPrefixKey = Constant.CartInfo.CART_PREFIX + memberId;
            if (!checkTempCartEmpty(tempCartPrefixKey)){
                //合并
                // 先将临时购物车的数据合并到用户购物车中;
            }

            saveCartItemToRedisCart(loginUserCartPrefixKey,skuId,num);


        } else {
            //add 2 临时
            saveCartItemToRedisCart(tempCartPrefixKey,skuId,num);
        }


        Map<String,Object> map = Maps.newHashMap();
        map.put("userkey",tempCartPrefixKey.substring(Constant.CartInfo.CART_PREFIX.length()));

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

    //2、添加购物车之前先确定购物车中有没有这个商品，如果有就数量+1 如果没有新增
    private void saveCartItemToRedisCart(String hashPrefixKey,Long skuId,Integer num){
        RMap<Object, Object> map = redissonClient.getMap(hashPrefixKey);
        String skuStr = (String) map.get(skuId.toString());
        if (StringUtils.isBlank(skuStr)){
            //从远程查看skuId对应的sku相关信息，并封装到cartItem中，存储到redis
            //todo 从远程查看skuId对应的sku相关信息
            ServerResponse<SkuInfoVo> skuInfoResponse = skuFeignService.getSKuInfoForCart(skuId);
            if (!skuInfoResponse.isSuccess()) {
               throw new SaveCartItemToRedisCartException("添加商品到购物车失败，查询商品信息失败");
            }
            SkuInfoVo skuInfoVo = skuInfoResponse.getData();
            CartItemVo cartItemVo = new CartItemVo();
            BeanUtils.copyProperties(skuInfoVo,cartItemVo);
            cartItemVo.setNum(num);

            map.put(skuId.toString(),JsonUtil.obj2Json(cartItemVo));
        } else {
            CartItemVo cartItemVo = JsonUtil.Json2Obj(skuStr, CartItemVo.class);
            cartItemVo.setNum(cartItemVo.getNum()+num);
            map.put(skuId.toString(),JsonUtil.obj2Json(cartItemVo));
        }
        //

    }



    public boolean checkTempCartEmpty(String redisCartKey) {
//        redisTemplate.opsForHash().delete();
        RMap<Object, Object> cartItemsOfCartMap = redissonClient.getMap(redisCartKey);
        Set<Object> itemSet = cartItemsOfCartMap.keySet();
        if (CollectionUtils.isEmpty(itemSet))
            return true;
        return false;
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