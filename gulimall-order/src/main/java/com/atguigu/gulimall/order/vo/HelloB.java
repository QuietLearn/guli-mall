package com.atguigu.gulimall.order.vo;

class HelloA {
    public HelloA(){
        System.out.println("hello A");
    }

    static {
        System.out.println("static A");
    }

    {
        System.out.println("I'm A class");
    }
}

public class HelloB extends HelloA{
    public HelloB(){
        System.out.println("hello B");
    }
    {
        System.out.println("I'm B class");
    }

    static {
        System.out.println("static B");
    }

    public static void main(String[] args) {
        System.out.println("start");
        new HelloB();
        new HelloB();
        System.out.println("end");
    }
}
