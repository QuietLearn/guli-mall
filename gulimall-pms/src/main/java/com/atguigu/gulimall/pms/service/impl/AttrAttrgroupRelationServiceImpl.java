package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.pms.vo.req.AttrRelationDeleteVo;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.Query;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import com.atguigu.gulimall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.pms.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public ServerResponse deleteAttrAndRelation(AttrRelationDeleteVo[] attrRelationDeleteVo) {
        if (attrRelationDeleteVo != null && attrRelationDeleteVo.length > 0) {
            for (AttrRelationDeleteVo deleteVo : attrRelationDeleteVo) {
                this.remove(
                        new QueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_group_id",deleteVo.getAttrGroupId())
                                .eq("attr_id",deleteVo.getAttrId()));
            }
        }

        return ServerResponse.createBySuccessMessage("删除属性与组关系成功");
    }

}