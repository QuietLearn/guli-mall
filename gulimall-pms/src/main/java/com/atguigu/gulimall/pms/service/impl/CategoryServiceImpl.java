package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.gulimall.commons.bean.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.pms.dao.CategoryDao;
import com.atguigu.gulimall.pms.entity.CategoryEntity;
import com.atguigu.gulimall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    CategoryDao extends baseMapper
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageVo(page);
    }

    @Override
    public ServerResponse<List<CategoryEntity>> listCategoryTree(Integer level) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (level!=0){
            queryWrapper.eq("cat_level",level);
        }

        List<CategoryEntity> categoryEntityList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(categoryEntityList)){
            return ServerResponse.createByErrorMessage("未查找任何三级分类，请添加");
        }
        return ServerResponse.createBySuccess("查询三级分类成功",categoryEntityList);
    }

    @Override
    public ServerResponse<List<CategoryEntity>> getChildCategory(Integer catId) {
        if (catId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR);
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        if (catId!=0){
            queryWrapper.eq("parent_cid",catId);
        }

        List<CategoryEntity> categoryEntityList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(categoryEntityList)){
            return ServerResponse.createByErrorMessage("该分类下没有任何子分类");
        }
        return ServerResponse.createBySuccess("查询子分类成功",categoryEntityList);
    }


    @Override
    public ServerResponse<List<Long>> getDeepCategory(Integer catId) {
        if (catId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR);
        }

        HashSet<CategoryEntity> categorySet = Sets.newHashSet();
        getCategoryKidBelowSet(categorySet,catId);

        List<Long> categoryIdList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categorySet)){ //其实不用判断因为[]不会再foreach取值时报空指针异常
            for (CategoryEntity categoryItem:categorySet) {
                categoryIdList.add(categoryItem.getCatId());//因为list有序，查找?比较方便，hashset只是用来排重
            }
            return ServerResponse.createBySuccess(categoryIdList);
        }
        return ServerResponse.createByErrorMessage("没有查询到该品类和下面的子品类");
    }


    //直接用set递归降低耦合，查什么放什么
    //返回值和参数一致，递归较容易，且是左遍历
    private Set<CategoryEntity> getCategoryKidBelowSet(Set<CategoryEntity> categorySet, Integer categoryId) {
        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cat_id",categoryId);
        CategoryEntity categoryEntity = baseMapper.selectOne(queryWrapper);
        if (categoryEntity!=null){
            //递归最最要的也是把最上面的parent 父id先加入
            categorySet.add(categoryEntity);
        }

        //查找子节点,递归算法一定要有一个退出的条件
        QueryWrapper<CategoryEntity> queryCategoryKidBelowWrapper = new QueryWrapper<>();
        queryCategoryKidBelowWrapper.eq("parent_cid",categoryId);
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(queryCategoryKidBelowWrapper);
        for (CategoryEntity categoryItem : categoryEntityList) {
            //递归遍历因为永远有指针指向set的堆内存区域，所以set增加是可以一直存在的，所以可以不写返回值
            getCategoryKidBelowSet(categorySet,categoryItem.getCatId().intValue());
        }
        return categorySet;
    }
}