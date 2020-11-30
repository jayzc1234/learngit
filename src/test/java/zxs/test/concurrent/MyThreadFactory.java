package zxs.test.concurrent;

import java.util.concurrent.ThreadFactory;

public class MyThreadFactory implements ThreadFactory {

    private String threadName;

    public  MyThreadFactory(String threadName){
        this.threadName = threadName;
    }
    public void setThreadName(String threadName){
      this.threadName = threadName;
    }
    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r,threadName);
    }
}
