package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.gulimall.commons.bean.*;
import com.atguigu.gulimall.pms.common.Const;
import com.atguigu.gulimall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.pms.dao.AttrGroupDao;
import com.atguigu.gulimall.pms.dao.CategoryDao;
import com.atguigu.gulimall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.pms.entity.AttrGroupEntity;
import com.atguigu.gulimall.pms.entity.CategoryEntity;
import com.atguigu.gulimall.pms.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.pms.vo.req.AttrGidVo;
import com.atguigu.gulimall.pms.vo.resp.AttrGroupVo;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.chrono.AssembledChronology;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.pms.dao.AttrDao;
import com.atguigu.gulimall.pms.entity.AttrEntity;
import com.atguigu.gulimall.pms.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Attr;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AttrGroupDao groupDao;

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }


    @Override
    public ServerResponse selectAttrByCategoryId(QueryCondition queryCondition,Long categoryId,int attrType) {
        if (categoryId==null||categoryId<=0){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR);
        }

        CategoryEntity categoryEntity = categoryDao.selectById(categoryId);

        if (categoryEntity==null){
            return ServerResponse.createByErrorMessage("该分类不存在");
        }


        IPage<AttrEntity> page = new Query<AttrEntity>().getPage(queryCondition);

//        建议建立attr和category的多对多关系表，联表查询出来结果
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper();
        queryWrapper.eq("catelog_id",categoryId);
        queryWrapper.eq("attr_type",attrType);

        IPage<AttrEntity> attrEntityIPage = baseMapper.selectPage(page, queryWrapper);

        if (CollectionUtils.isEmpty(attrEntityIPage.getRecords())){
            ServerResponse.createByErrorMessage("该分类下没有对应基本属性");
        }
        return  ServerResponse.createBySuccess("查询该三级分类下的所有销售属性成功",new PageVo(attrEntityIPage));
    }


    @Override
    public ServerResponse selectAttrByGroupId(QueryCondition queryCondition, Long groupId,Integer attrType) {
        if (groupId==null||groupId<=0){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR);
        }

        AttrGroupEntity attrGroup = groupDao.selectById(groupId);

        if (attrGroup==null){
            return ServerResponse.createByErrorMessage("该属性分组不存在");
        }

        IPage<AttrEntity> page = new Query<AttrEntity>().getPage(queryCondition);


        List<AttrEntity> attrEntities = baseMapper.selectAttrByGroupId(attrType, groupId);
        if (CollectionUtils.isEmpty(attrEntities)){
            ServerResponse.createByErrorMessage("该分組下没有对应基本属性");
        }


        return ServerResponse.createBySuccess("查询分类下基本属性成功",new PageVo(page.setRecords(attrEntities)));
    }


    @Override
    public ServerResponse selectAttrInfoWithGroup(Long attrId) {
        if (attrId==null||attrId<=0){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR);
        }
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper();
        queryWrapper.eq("attr_id",attrId);
        AttrEntity attrEntity = baseMapper.selectOne(queryWrapper);
        if (attrEntity==null){
            return ServerResponse.createByErrorMessage("该属性不存在");
        }

        QueryWrapper<AttrAttrgroupRelationEntity> aagrQueryWrapper = new QueryWrapper();
        aagrQueryWrapper.select("attr_group_id");
        aagrQueryWrapper.eq("attr_id",attrEntity.getAttrId());
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(aagrQueryWrapper);

        AttrGroupEntity attrGroup=null;
        if (attrAttrgroupRelationEntity!=null){
            Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
            attrGroup = groupDao.selectById(attrGroupId);
        }

        AttrGroupVo attrGroupVo = new AttrGroupVo();

        BeanUtils.copyProperties(attrEntity,attrGroupVo);

        attrGroupVo.setGroup(attrGroup);
        String msg = attrGroup==null?"该属性没有所属分组":"查询属性及其分组成功";

        return  ServerResponse.createBySuccess(msg,attrGroupVo);
    }

    @Transactional
    @Override
    public ServerResponse<Object> saveAttr(AttrGidVo attrGidVo) {
//        我个人感觉分类和属性的关系也是多对多，所以是另外表存储的，需要判断该分类存不存在
        if (attrGidVo==null){
            ServerResponse.createByErrorMessage("没有传参");
        }
        AttrEntity attrEntity = new AttrEntity();

        BeanUtils.copyProperties(attrGidVo,attrEntity);
        Long attrGroupId = attrGidVo.getAttrGroupId();
        AttrGroupEntity attrGroupEntity = groupDao.selectById(attrGroupId);
//        分组可以不存在，到时候 该分类直接查出对应分类下的所有属性即可，有些属性是没有分组的
        /*if (attrGroupEntity==null){
            return ServerResponse.createByErrorMessage(attrGroupId+"分组不存在");
        }*/

        int attrInsertCount = baseMapper.insert(attrEntity);
        if (attrInsertCount<=0){
            return ServerResponse.createByErrorMessage("插入attr失败");
        }

//        因为mp自己实现了返回生成的主键
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = assemAttrAttrgroupRelationEntity(attrGroupId, attrEntity.getAttrId());

        int attrAttrgroupInsertCount = attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        if (attrAttrgroupInsertCount<=0){
            return ServerResponse.createByErrorMessage("插入attrAttrgroup失败");
        }
        return ServerResponse.createBySuccessMessage("插入attr及关联表attrAttrgroup数据成功");

    }

    private AttrAttrgroupRelationEntity assemAttrAttrgroupRelationEntity(Long attrGroupId,Long attrId){
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrGroupId(attrGroupId);
        attrAttrgroupRelationEntity.setAttrId(attrId);
        attrAttrgroupRelationEntity.setAttrSort(0);
        return attrAttrgroupRelationEntity;
    }

}