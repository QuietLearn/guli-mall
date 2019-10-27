package com.atguigu.gulimall.wms.vo;


import lombok.Data;

import java.util.List;

/**
 * 库存锁定的信息
 */
@Data
public class LockStockVo {

    private List<SkuLock> locks;

    //【调货】：从不同的仓库调货，因为有可能不同仓库货源数量都有一点
    //拆单逻辑；订单里面有很多的商品来源于不同的仓库，以仓库发货为单位进行拆分
    //1.1 把订单拆开，分开分批发货
    private Boolean locked;//最终锁定成功还是失败。 比如锁的商品比较多，最终是怎么样的
}


