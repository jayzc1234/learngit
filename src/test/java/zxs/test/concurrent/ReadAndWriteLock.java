package zxs.test.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadAndWriteLock {
    private static Map<String,String> map=new HashMap<>();
    private static ReentrantReadWriteLock rwLock=new ReentrantReadWriteLock();
    static ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    static ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
    static ReentrantLock reentrantLock=new ReentrantLock();
    static Condition condition = reentrantLock.newCondition();


    public static void main(String[] args) throws InterruptedException {
        Thread thread1=new Thread(new Runnable() {
            @Override
            public void run() {
                lock1();
                System.out.println("执行thread1 run");
            }
        });

        Thread thread2=new Thread(new Runnable() {
            @Override
            public void run() {
                lock2();
                System.out.println("执行thread2 run");
            }
        });

        Thread thread3=new Thread(new Runnable() {
            @Override
            public void run() {
                lock3();
                System.out.println("执行thread3 run");
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();

        Thread.sleep(2000);
        reentrantLock.lock();
        condition.signal();
        reentrantLock.unlock();
        Scanner scanner=new Scanner( System.in);
        String s = scanner.nextLine();
        if (s.equalsIgnoreCase("C")){
            condition.signal();
        }
    }
    public static final void lock1(){
        reentrantLock.lock();
        try {
            System.out.println("执行lock1");
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            reentrantLock.unlock();
        }
    }

    public static final void lock2(){
        reentrantLock.lock();
        try {
            System.out.println("执行lock2");
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            reentrantLock.unlock();
        }
    }

    public static final void lock3(){
        reentrantLock.lock();
        try {
            System.out.println("执行lock3");
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            reentrantLock.unlock();
        }
    }
    public static final String test(String key){
        reentrantLock.lock();
        try {
            String s = map.get(key);
            return s;
        } finally {
            reentrantLock.unlock();
        }
    }


    public static final String get(String key){
        readLock.lock();
        try {
            String s = map.get(key);
            return s;
        }finally {
            readLock.unlock();
        }
    }

    public static final void set(String key,String value){
        writeLock.lock();
        try {
            map.put(key,value);
        }finally {
            writeLock.unlock();
        }
    }
}
