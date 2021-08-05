package com.zxs;

public class Test2 {
    private int index;

    public void m1(){
        int a = index;
        System.out.println(a);
        int b =a;
    }
    public void m2(){
        System.out.println(index);
        int b = index;
    }

    public static void main(String[] args) {

        int i = 512 >>> 4;
        System.out.println(i);
    }

}
