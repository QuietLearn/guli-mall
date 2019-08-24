package com.atguigu.gulimall.commons.utils;

import java.util.Random;

public class RandomDataUtil {

    public static String generateSixRandomNum(){
        return String.valueOf(new Random().nextInt(999999));
    }

    public static String generateRandomNum(int size){
        String randomNum="";
        for (int i = 0; i < size; i++) {
            randomNum = randomNum + new Random().nextInt(9);
        }
        return randomNum;
    }

}
