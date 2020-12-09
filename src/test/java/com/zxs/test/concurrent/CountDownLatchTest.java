package com.zxs.test.concurrent;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.countDown();
        countDownLatch.await();
        Thread thread = new Thread();
        thread.isInterrupted();
    }

}
