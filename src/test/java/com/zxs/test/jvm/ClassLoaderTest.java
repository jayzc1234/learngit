package com.zxs.test.jvm;

public class ClassLoaderTest {

    public static void main(String[] args) throws ClassNotFoundException {
        CustomClassLoader customClassLoader = new CustomClassLoader(null);
        Class<?> aClass = customClassLoader.loadClass("com.zxs.pojo.Person");
        System.out.println(aClass);


    }
}
