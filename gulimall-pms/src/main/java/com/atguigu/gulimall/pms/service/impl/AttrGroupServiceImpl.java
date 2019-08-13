package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.gulimall.commons.bean.*;
import com.atguigu.gulimall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.pms.dao.AttrDao;
import com.atguigu.gulimall.pms.dao.CategoryDao;
import com.atguigu.gulimall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.pms.entity.AttrEntity;
import com.atguigu.gulimall.pms.entity.CategoryEntity;
import com.atguigu.gulimall.pms.vo.resp.AttrgroupWithAttrsVo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.pms.dao.AttrGroupDao;
import com.atguigu.gulimall.pms.entity.AttrGroupEntity;
import com.atguigu.gulimall.pms.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private AttrDao attrDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public ServerResponse<PageVo> listAttrGroupWithAttrsByCatId(QueryCondition queryCondition, Long catId,Integer attrType) {
        if (catId==null||catId<=0){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR);
        }
        CategoryEntity categoryEntity = categoryDao.selectById(catId);

        if (categoryEntity==null){
            return ServerResponse.createByErrorMessage("该"+catId+"分类不存在");
        }

        IPage<AttrGroupEntity> page = new Query<AttrGroupEntity>().getPage(queryCondition);


        QueryWrapper<AttrGroupEntity> attrGroupQueryWrapper = new QueryWrapper();
        attrGroupQueryWrapper.select("attr_group_id");
        attrGroupQueryWrapper.eq("catelog_id",categoryEntity.getCatId());

        IPage attrGroupEntityIPage = baseMapper.selectPage(page, attrGroupQueryWrapper);

        List<AttrGroupEntity> attrGroupList = attrGroupEntityIPage.getRecords();
        if (CollectionUtils.isEmpty(attrGroupList)){
            ServerResponse.createByErrorMessage("该分类下没有对应的属性分组");
        }

//        List<Object> attrGroupIdList = baseMapper.selectObjs(attrGroupQueryWrapper);

        List<AttrgroupWithAttrsVo> attrgroupWithAttrsVoList = Lists.newArrayList();

        attrGroupList.forEach(attrGroup->{
            AttrgroupWithAttrsVo attrgroupWithAttrsVo = this.selectAttrGroupWithattrs(attrGroup.getAttrGroupId(),attrType).getData();
            attrgroupWithAttrsVoList.add(attrgroupWithAttrsVo);
        });


        attrGroupEntityIPage.setRecords(attrgroupWithAttrsVoList);

        return ServerResponse.createBySuccess("查询分类下属性分组成功",new PageVo(attrGroupEntityIPage));
    }

    @Override
    public ServerResponse<AttrgroupWithAttrsVo> selectAttrGroupWithattrs(Long attrGroupId,Integer attrType) {
        if (attrGroupId==null||attrGroupId<=0){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR);
        }

        AttrGroupEntity attrGroupEntity = baseMapper.selectById(attrGroupId);
        if (attrGroupEntity==null){
            return ServerResponse.createByErrorMessage("该属性分组不存在");
        }

        QueryWrapper<AttrAttrgroupRelationEntity> aagrQueryWrapper = new QueryWrapper();
        aagrQueryWrapper.select("attr_id");
        aagrQueryWrapper.eq("attr_group_id",attrGroupEntity.getAttrGroupId());
        List<Object> attrIdList = attrAttrgroupRelationDao.selectObjs(aagrQueryWrapper);



        AttrgroupWithAttrsVo attrGroupWithAttrsVo = new AttrgroupWithAttrsVo();
        BeanUtils.copyProperties(attrGroupEntity,attrGroupWithAttrsVo);

        if (!CollectionUtils.isEmpty(attrIdList)){
            QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().in("attr_id", attrIdList);
            if (attrType!=null){
                queryWrapper.eq("attr_type",attrType);
            }

            List<AttrEntity> attrEntityList = attrDao.selectList(queryWrapper);
            attrGroupWithAttrsVo.setAttrEntities(attrEntityList);
        }

        return ServerResponse.createBySuccess("查旬分组以及分组下面的所有属性信息",attrGroupWithAttrsVo);
    }

}