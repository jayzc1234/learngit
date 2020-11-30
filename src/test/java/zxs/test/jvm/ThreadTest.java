package zxs.test.jvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ThreadTest {

    public static void main(String[] args) throws IOException {
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        br.readLine();
        System.out.println("执行busyThread方法");
        createBusyThread();

        br.readLine();
        System.out.println("执行createLockThread方法");
        Object obj=new Object();
        createLockThread(obj);
    }

    public static void createBusyThread(){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
               while (true){

               }
            }
        },"testBusyThread");
        thread.start();
    }

    public static void createLockThread(final Object lock){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
              synchronized (lock){
                  try {
                      lock.wait();
                  }catch (Exception e){
                     e.printStackTrace();
                  }
              }
            }
        },"testLockThread");
        thread.start();
    }


















}
