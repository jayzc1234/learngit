package com.zxs.test.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class CyclicBarrierTest {

    private static HelloWorld helloWorld = new HelloWorld();

    public static void main(String[] args) throws InterruptedException {
        CyclicBarrierTest cyclicBarrierTest = new CyclicBarrierTest();
        CyclicBarrierTest cyclicBarrierTest2 = new CyclicBarrierTest();
    }

    private static void getThread() throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2,()->{
            String name = Thread.currentThread().getName();
            System.out.println("all mission is done ");
            throw new RuntimeException();
        });

        Thread thread1 = new Thread(new Task(cyclicBarrier));
        Thread thread2 = new Thread(new Task(cyclicBarrier));
        thread1.start();

        Thread.sleep(50000);
        thread2.start();

        thread1.join();
        thread2.join();

        Semaphore semaphore = new Semaphore(1);

        semaphore.acquire();
        semaphore.acquire();
    }

}
class Task implements Runnable{

    CyclicBarrier cyclicBarrier;

    public Task(CyclicBarrier cyclicBarrier){
        this.cyclicBarrier = cyclicBarrier;
    }
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" is running");
        try {
            cyclicBarrier.await();
            System.out.println(Thread.currentThread().getName()+" is over");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
