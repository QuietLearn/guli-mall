package com.atguigu.gulimall.pms.vo.resp;

import com.atguigu.gulimall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.pms.entity.AttrEntity;
import com.atguigu.gulimall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;


@Data
public class AttrgroupWithAttrsVo extends AttrGroupEntity {

    private List<AttrEntity> attrEntities;

//    private List<AttrAttrgroupRelationEntity> relations;

}
