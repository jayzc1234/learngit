package com.zxs.test.jvm;

public class InitTest1 {

    private static InitTest1 initTest1 = new InitTest1();
    public InitTest1(){
        counter ++;
    }
    private static int counter = 0;

    public int getCounter(){
        return counter;
    }

    public static InitTest1 getInitTest1(){
        return initTest1;
    }
    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> aClass = initTest1.getClass().getClassLoader().loadClass("com.zxs.test.jvm.TestClassLoading");
        System.out.println(aClass);


//        Map<Integer,String> map =new HashMap<>();
//        String jay = map.putIfAbsent(1, "jay");
//        System.out.println(jay);
//
//        System.out.println(System.getProperty("sun.boot.class.path"));
//        System.out.println(System.getProperty("java.ext.dirs"));
//        System.out.println(System.getProperty("java.class.path"));
//        System.out.println(System.getProperty("java.system.class.loader"));
//        InitTest1 initTest11 = getInitTest1();
//        int counter1 = initTest11.getCounter();
//        InitTest1 initTest1 = new InitTest1();
//        int counter = initTest1.getCounter();
//        System.out.println(counter);
    }


}
