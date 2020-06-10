package com.zxs.test.concurrent;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 数组阻塞队列，从数组最右边向左入队，从左向右出队
 * 定义两个指针分别是出队指针和入队指针
 * @param <T>
 */
public class MArrayBlockingQueue<T> implements Iterable<T>{

    private Object items[];

    private int putIndex;

    private int takeIndex;

    private int capacity;

    private AtomicInteger size=new AtomicInteger(0);

    public MArrayBlockingQueue(int size){
        items=new Object[size];
        this.capacity=size;
        putIndex=capacity;
        takeIndex=capacity;
        LinkedBlockingQueue queue=new LinkedBlockingQueue();
        queue.size();
    }

    public boolean isEmpty(){
        return size.get()==0;
    }

    private ReentrantLock putLock=new ReentrantLock();

    private ReentrantLock takeLock=new ReentrantLock();

    private Condition notFull=putLock.newCondition();

    private Condition notEmpty=takeLock.newCondition();


    public void add (T t) throws InterruptedException {
        try {
            putLock.lock();
            if (size.get()==capacity){
               notFull.await();
            }
            System.out.println(Thread.currentThread().getName()+"入队："+t);
            items[--putIndex]=t;
            size.getAndIncrement();

            if (putIndex==0){
                putIndex=capacity-1;
            }
            notFull.signal();
        }finally {
            putLock.unlock();
        }

        if (size.get()>=capacity/2){
            try {
                takeLock.lock();
                notEmpty.signalAll();
            }finally {
                takeLock.unlock();
            }
        }
    }

    public T poll () throws InterruptedException {
        T e;
        try {
            takeLock.lock();
            if (size.get()==0){
                notEmpty.await();
            }
            if (takeIndex==0){
                takeIndex=capacity-1;
            }
            e= (T) items[--takeIndex];
            System.out.println(Thread.currentThread().getName()+"出队："+e);
            size.getAndDecrement();
            notEmpty.signal();

        }finally {
            takeLock.unlock();
        }
        if (size.get()<=capacity/2){
            try {
                putLock.lock();
                notFull.signalAll();
            }finally {
                putLock.unlock();
            }
        }
        return (T) e;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super T> action) {

    }

    @Override
    public Spliterator<T> spliterator() {
        return null;
    }
}
