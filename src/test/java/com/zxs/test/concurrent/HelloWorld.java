package com.zxs.test.concurrent;

public class HelloWorld {

    public HelloWorld(){
        System.out.println("hello ");
    }
    public  String sayHello(){
        return "Hello Maven";
    }
    public static void main(String[] args) {
        System.out.println(new HelloWorld().sayHello());
    }
}
