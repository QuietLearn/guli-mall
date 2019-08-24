package com.atguigu.gulimall.ums.dao;

import com.atguigu.gulimall.ums.entity.MemberPhoneCode;
import org.springframework.stereotype.Repository;


@Repository
public interface TMemberPhoneCodeMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(MemberPhoneCode record);

    int insertSelective(MemberPhoneCode record);

    MemberPhoneCode selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MemberPhoneCode record);

    int updateByPrimaryKey(MemberPhoneCode record);
}