package com.zxs.zxs.mybatisplus;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QueryTest {

    public static void main(String[] args) throws InterruptedException {
//        LambdaQueryWrapper<Main> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.groupBy(Main::getAa);
//        TypeVariable<? extends Class<? extends LambdaQueryWrapper>>[] typeParameters = lambdaQueryWrapper.getClass().getTypeParameters();
//        for (TypeVariable<? extends Class<? extends LambdaQueryWrapper>> typeParameter : typeParameters) {
//            System.out.println(typeParameter);
//        }
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,1,1L, TimeUnit.MINUTES,new ArrayBlockingQueue<Runnable>(1));
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(1);
            }
        });

        ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
        threadLocal.set(1);
        Integer integer = threadLocal.get();



    }
}
