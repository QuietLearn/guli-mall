package com.atguigu.gulimall.pms.controller;

import java.util.Arrays;


import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.QueryCondition;
import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.pms.common.Const;
import com.atguigu.gulimall.pms.vo.resp.AttrgroupWithAttrsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.pms.entity.AttrGroupEntity;
import com.atguigu.gulimall.pms.service.AttrGroupService;




/**
 * 属性分组
 *
 * @author heyijie
 * @email lfy@atguigu.com
 * @date 2019-08-01 15:52:32
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;




    /**
     * 查询某个三级分类下的所有属性分组
     *
     * 极其建议和新建spu及相关信息 列出对应分类分组属性和方便选取的建议值  的接口分开
     *
     * 因为在分组、基本属性和销售属性列表页面 数据重复了，带宽增大，要么1个，要么3个分开
     */
    @ApiOperation("查询某个三级分类下的所有属性分组")
    @GetMapping("/list/category/{catId}")
    public ServerResponse<PageVo> listAttrGroupByCatId(QueryCondition queryCondition, @PathVariable("catId") Long catId) {

        return attrGroupService.listAttrGroupWithAttrsByCatId(queryCondition,catId, Const.AttrType.BASE_ATTR);
    }

    /**
     * 查询某个分组以及分组下面的所有属性信息
     */
    @ApiOperation("查询某个分组以及分组下面的所有属性信息")
    @GetMapping("/info/withattrs/{attrGroupId}")
    public ServerResponse<AttrgroupWithAttrsVo> selectAttrGroupWithattrs(@PathVariable("attrGroupId") Long attrGroupId) {

        return attrGroupService.selectAttrGroupWithattrs(attrGroupId,null);
    }


    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}
