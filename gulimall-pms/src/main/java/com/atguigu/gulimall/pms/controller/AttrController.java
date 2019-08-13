package com.atguigu.gulimall.pms.controller;

import java.util.Arrays;
import java.util.Map;


import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.QueryCondition;
import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.pms.common.Const;
import com.atguigu.gulimall.pms.vo.req.AttrGidVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.pms.entity.AttrEntity;
import com.atguigu.gulimall.pms.service.AttrService;




/**
 * 商品属性
 *
 * @author heyijie
 * @email lfy@atguigu.com
 * @date 2019-08-01 15:52:32
 */
@Api(tags = "商品属性 管理")
@RestController
@RequestMapping("pms/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;


    /**
     * 查询某个三级分类下的所有基本属性
     */
    @ApiOperation("查询某个三级分类下的所有基本属性")
    @GetMapping("/base/{categoryId}")
    public ServerResponse selectBaseAttrByCategory(QueryCondition queryCondition,@PathVariable("categoryId") Long categoryId) {

        return attrService.selectAttrByCategoryId(queryCondition,categoryId, Const.AttrType.BASE_ATTR);
    }

    /**
     * 查询某个三级分类下的所有销售属性
     */
    @ApiOperation("查询某个三级分类下的所有销售属性")
    @GetMapping("/sale/{categoryId}")
    public ServerResponse selectSaleAttrByCategory(QueryCondition queryCondition,@PathVariable("categoryId") Long categoryId) {

        return attrService.selectAttrByCategoryId(queryCondition,categoryId, Const.AttrType.SALE_ATTR);
    }


    /**
     * 查询某个分组下对应的所有属性
     */
    @ApiOperation("查询某个分组下对应的所有属性")
    @GetMapping("/group/list/{groupId}")
    public ServerResponse selectBaseAttrByGroup(QueryCondition queryCondition,@PathVariable("groupId") Long groupId,Integer attrType) {

        return attrService.selectAttrByGroupId(queryCondition,groupId,attrType);
    }



    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attr:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 查出某个属性，以及这个属性所属的分组信息
     */
    @ApiOperation("查出某个属性，以及这个属性所属的分组信息")
    @GetMapping("/info/{attrId}")
    @PreAuthorize("hasAuthority('pms:attr:info')")
    public ServerResponse selectAttrInfoWithGroup(@PathVariable("attrId") Long attrId) {

        return attrService.selectAttrInfoWithGroup(attrId);
    }


    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attr:save')")
    public ServerResponse<Object> save(@RequestBody AttrGidVo attrGidVo){


        return attrService.saveAttr(attrGidVo);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
//    @PutMapping("/update/{attrId}"),@PathVariable("attrId") Long attrId 前端也需要改为put提交干
    @PostMapping("/update/{attrId}")
    @PreAuthorize("hasAuthority('pms:attr:update')")
    public Resp<Object> update(@RequestBody AttrEntity attr){
		attrService.updateById(attr);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attr:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return Resp.ok(null);
    }

}
