package zxs.test.concurrent;

import java.util.ArrayList;
import java.util.List;

public class VolatileTest {
    private  List<Integer> nums=new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        VolatileTest volatileTest=new VolatileTest();
        volatileTest.test();
    }

    private  void test() throws InterruptedException {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("生产数据线程已开启");
                for (int i=0;i<5;i++){
                    VolatileTest.this.nums.add(i);
                    System.out.println("生产数据线程向nums添加 ："+i);
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread thread2=new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("消费数据线程已开启");
                for (int i=0;i<5;i++){
                    try {
                        Integer remove = VolatileTest.this.nums.get(i);
                        System.out.println("消费数据线程从nums "+i+"处消费 ："+remove);
                    }catch (Exception e){

                    }
                }
            }
        });
        thread.start();
        Thread.sleep(1000);
        thread2.start();
    }
}
