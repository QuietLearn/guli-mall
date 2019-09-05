package com.atguigu.guli.cart.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 整个购物车
 */

public class CartVo {

    private Integer totalCount; //总商品数量

    private BigDecimal totalPrice;//总商品价格

    private BigDecimal reductionPrice;//优惠了的价格

    private BigDecimal cartPrice;//购物车应该支付的价格

    @Getter
    @Setter
    private List<CartItemVo> items;//购物车中所有的购物项；

    @Getter
    @Setter
    private String userKey;//临时用户的key

    /**
     * 只选择的数量， 前端还有购物车有几个item 的计算，这个前端直接通过获取item数组，计算size即可
     * @return
     */
    public Integer getTotalCount() {
        Integer num = 0;
        if (items != null && items.size() > 0) {
            for (CartItemVo item : items) {
                //如果没选中的商品不进入计算
                if(!item.isCheck()){
                    continue;
                }
                num += item.getNum();
            }
        }
        return num;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal decimal = new BigDecimal("0.0");

        if (items != null && items.size() > 0) {
            for (CartItemVo item : items) {
                //如果没选中的商品不进入计算
                if(!item.isCheck()){
                    continue;
                }
                BigDecimal totalPrice = item.getTotalPrice();

                decimal = decimal.add(totalPrice);
            }
        }


        return decimal;
    }


    //减多少
    public BigDecimal getReductionPrice() {
        BigDecimal reduce = new BigDecimal("0.0");
        //拿到每一项的满减信息和优惠信息
        if (CollectionUtils.isEmpty(items))
            return new BigDecimal("0.0");
        for (CartItemVo item : items) {
            //如果没选中的商品不进入计算
            if(!item.isCheck()){
                continue;
            }

            List<SkuFullReductionVo> reductions = item.getReductions();


            // todo 便于计算最大优惠，增加用户体验
            LinkedBlockingDeque<SkuFullReductionVo> fullReductionVos = new LinkedBlockingDeque<>();
            if (!CollectionUtils.isEmpty(reductions)) {
                for (SkuFullReductionVo reduction : reductions) {
                    if (reduction.getAddOther() == 1) {
                        //代表可以叠加优惠
                        fullReductionVos.addFirst(reduction);
                    } else {
                        fullReductionVos.addLast(reduction);
                    }
                }
            }

            //给队尾放数据
            //reductionVos.put();N

//            reductionVos.add(null);
            //从队头拿出元素但不删除。
//            SkuFullReductionVo peek = reductionVos.peek();

            //从队头移除和获取一个元素
//            SkuFullReductionVo poll = reductionVos.poll();
            //
//            SkuFullReductionVo take = reductionVos.take();

            //计算满减打折等可以减掉的金额
//            reductions!=null&&reductions.size()>0
            if (!CollectionUtils.isEmpty(reductions)) {
//                for (SkuFullReductionVo reduction : reductions) {
                for (SkuFullReductionVo reduction : fullReductionVos) {

                    Integer type = reduction.getType();

                    Integer addOther = reduction.getAddOther();

                    if (type == 0) {
                        //0-打折  1-满减
                        Integer fullCount = reduction.getFullCount();
                        Integer discount = reduction.getDiscount();
                        if (item.getNum() >= fullCount) {
                            //折后价 100 98 10 1%100 = 100 98
                            // 100/100 = 1.0
                            // 98/100 = 0.98
                            BigDecimal reduceTp = item.getTotalPrice().multiply(new BigDecimal((discount/100)+"." + (discount%100)));
                            //减了这么多
                            BigDecimal subtract = item.getTotalPrice().subtract(reduceTp);
                            //累加了折后价
                            reduce = reduce.add(subtract);
                        }
                    }
                    if (type == 1) {
                        //最好判断一下是否为空
                        BigDecimal fullPrice = reduction.getFullPrice();
                        BigDecimal reducePrice = reduction.getReducePrice();

                        //-1 0 1
//                        if ((item.getTotalPrice().subtract(fullPrice).compareTo(new BigDecimal("0.0")) > -1)) {
                        if ((item.getTotalPrice().compareTo(fullPrice) > -1)) {
                            //累加了优惠价
                            reduce = reduce.add(reducePrice);
                        }
                    }
                    if(addOther == 0){
                        //不能叠加优惠
                        break;
                    }
                }
            }


            //计算优惠券可以减掉的金额
            List<SkuCouponVo> coupons = item.getCoupons();
            if (coupons != null && coupons.size() > 0) {
                for (SkuCouponVo coupon : coupons) {
                    BigDecimal amount = coupon.getAmount();
                    reduce = reduce.add(amount);

                }
            }
        }

        return reduce;
    }

    //减后的价格
    public BigDecimal getCartPrice() {
        BigDecimal reductionPrice = getReductionPrice();
        BigDecimal totalPrice = getTotalPrice();
        BigDecimal subtract = totalPrice.subtract(reductionPrice);
        return subtract;
    }

    public static void main(String[] args) {
        //bigdecimal只对string保留精度，虽然有double的构造函数，但是精度无法保留，最好是string
        BigDecimal a = new BigDecimal("10.00");
        BigDecimal b = new BigDecimal("9.00");
        System.out.println(a.compareTo(b));
    }
}
