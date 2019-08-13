package com.atguigu.gulimall.wms.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.atguigu.gulimall.commons.bean.*;
import com.atguigu.gulimall.commons.to.wms.SkuStockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.wms.entity.WareSkuEntity;
import com.atguigu.gulimall.wms.service.WareSkuService;




/**
 * 商品库存
 *
 * @author heyijie
 * @email hyj78586421@outlook.com
 * @date 2019-08-01 18:46:30
 */
@Api(tags = "商品库存 管理")
@RestController
@RequestMapping("wms/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;



    @PostMapping("/skus/stock")
    @ApiOperation("获取skuIdList iD列表 对应的库存信息")
    public ServerResponse<List<SkuStockVo>> skuWareInfos(@RequestBody List<Long> skuIds){

        List<SkuStockVo> vos = new ArrayList<>();

        if (CollectionUtils.isEmpty(skuIds)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR);
        }

        for (Long skuId : skuIds) {
            QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("stock");
            queryWrapper.eq("sku_id",skuId);
            List<Object> stockList = wareSkuService.listObjs(queryWrapper);
            Integer stockSum=0;
            SkuStockVo vo = new SkuStockVo();
            for (Object stockObj : stockList) {
                Integer stock = (Integer) stockObj;
                stockSum+=stock;
            }

            vo.setStock(stockSum);
            vo.setSkuId(skuId);
            vos.add(vo);
        }

        if (CollectionUtils.isEmpty(vos)){
            return ServerResponse.createByErrorMessage("该skuIds未有对应的仓库 库存信息");
        }

        return ServerResponse.createBySuccess("查询skuIds 库存信息成功",vos);

    }

    /**
     * 获取某个sku的库存信息
     * @param skuId
     * @return
     */
    @ApiOperation("获取某个sku的库存信息")
    @GetMapping("/sku/{skuId}")
    public Resp<List<WareSkuEntity>> skuWareInfos(@PathVariable("skuId")Long skuId){

        List<WareSkuEntity> list = wareSkuService.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId));

        return Resp.ok(list);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('wms:waresku:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = wareSkuService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('wms:waresku:info')")
    public Resp<WareSkuEntity> info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return Resp.ok(wareSku);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('wms:waresku:save')")
    public Resp<Object> save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('wms:waresku:update')")
    public Resp<Object> update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('wms:waresku:delete')")
    public Resp<Object> delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return Resp.ok(null);
    }

}
