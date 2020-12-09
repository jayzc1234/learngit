package com.zxs.test.concurrent;

import java.util.concurrent.SynchronousQueue;

public class SynchronousQueueTest {
    public static void main(String[] args) throws InterruptedException {
        SynchronousQueue<Integer> synchronousQueue=new SynchronousQueue<>();
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    synchronousQueue.put(1);
                    System.out.println("加入1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        Thread.sleep(1000);
        Thread thread2=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Integer take = synchronousQueue.take();
                    System.out.println("获取的元素："+take);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread2.start();

//        synchronousQueue.put(2);
//        System.out.println("加入2");
//
//        synchronousQueue.put(1);
//        System.out.println("加入3");
    }

}
