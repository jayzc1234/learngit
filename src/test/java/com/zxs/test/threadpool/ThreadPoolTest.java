package com.zxs.test.threadpool;

import java.util.concurrent.*;

public class ThreadPoolTest {
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int RUNNING    = -1 << COUNT_BITS;
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;
    public static void main(String[] args) {
        System.out.println(COUNT_BITS);
        ThreadPoolTest poolTest=new ThreadPoolTest();
        PrintRunnable runnable=poolTest.new PrintRunnable(1);
        PrintRunnable runnable2=poolTest.new PrintRunnable(2);
        ExecutorService executorService=new ThreadPoolExecutor(1,2,2000L, TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>());
//        executorService.submit(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);
        Future<?> submit = executorService.submit(runnable);
        System.out.println("主线程结束");
        Executors.newCachedThreadPool();
    }



    class PrintRunnable implements Runnable{
        private int num;

        public PrintRunnable(int num){
            this.num=num;
        }
        @Override
        public void run() {
            System.out.println("PrintRunnable,num="+num);
        }
    }
}
