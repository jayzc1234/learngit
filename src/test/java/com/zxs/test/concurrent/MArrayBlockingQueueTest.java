package com.zxs.test.concurrent;


import java.util.Random;

public class MArrayBlockingQueueTest {
    public static void main(String[] args) throws InterruptedException {
        MArrayBlockingQueue<Integer> queue=new MArrayBlockingQueue(500);
//        queue.add(1);
//        queue.add(2);
//        queue.add(3);
//        queue.add(4);
//        queue.add(5);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(100);
//                    Integer poll1 = queue.poll();
//                    Integer poll2 = queue.poll();
//                    Integer poll3 = queue.poll();
//                    Integer poll4 = queue.poll();
//                    Integer poll5 = queue.poll();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        Random random=new Random(100);
        for (int i=0;i<8;i++){
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j<= finalI; j++){
                        int i1 = random.nextInt(100);
                        try {
                            queue.add(i1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            },"生产者线程："+i).start();
        }

        for (int i=0;i<8;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!queue.isEmpty()){
                            Integer poll = queue.poll();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            },"消费者线程："+i).start();
        }
    }
}
