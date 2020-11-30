package zxs.test.concurrent;

import lombok.Data;

import java.util.concurrent.locks.LockSupport;

@Data
public class SyncTest {
   private int a;
   public void test(){
       a++;
   }
    public synchronized void test2(){
        a++;
    }

    public static void main(String[] args) {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                LockSupport.park();
                System.out.println(1);
            }
        });
        thread.start();
        thread.interrupt();
        boolean interrupted = thread.isInterrupted();
        boolean interrupted1 = thread.isInterrupted();
        boolean interrupted2 = Thread.interrupted();
        System.out.println(interrupted+","+interrupted1);
    }
}
