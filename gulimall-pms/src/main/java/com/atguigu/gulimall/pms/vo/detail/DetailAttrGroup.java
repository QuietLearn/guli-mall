package com.atguigu.gulimall.pms.vo.detail;

import lombok.Data;

import java.util.List;

@Data
public class DetailAttrGroup {

   private Long groupId;

   private String groupName;

   private List<DetailBaseAttrVo> detailBaseAttrVoList;
}
