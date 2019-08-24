package com.atguigu.guli.cart.exception;

import net.bytebuddy.implementation.bind.annotation.Super;

public class SaveCartItemToRedisCartException extends RuntimeException {

    public SaveCartItemToRedisCartException(String msg){
        super(msg);
    }
}
