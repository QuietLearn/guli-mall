package com.atguigu.gulimall.pms;


import org.apache.commons.lang3.StringUtils;
import org.junit.Test;


public class StringTest {
    @Test
    public void fun1(){
        String a[]={"1","33","sfs"};
        String join = StringUtils.join(a, ",");
        System.out.println(join);
    }
}
