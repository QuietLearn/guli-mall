package com.atguigu.gulimall.order.vo;

public class ExceptionTest {
    public static void main(String[] args) {
        fn();
    }

    private static int[] fn(){
        try{
            int[] a = new int[3];
            a[3] = 0;
            return a;
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("out of bounds exception");
        } catch (RuntimeException e){
            System.out.println("runtime exception");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("exception");
        } finally {
            System.out.println("finally");
        }
        return null;
    }
}
