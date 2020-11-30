package zxs.test.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

public class LinkedTransferQueueTest {
    private static LinkedTransferQueue<Integer> tail=new LinkedTransferQueue<>();

    public static void main(String[] args) throws InterruptedException {
        LinkedTransferQueue<Integer> p=tail;
        tail=new LinkedTransferQueue<>();
        System.out.println(p==tail);
        doubleAdd();
    }



    private static void takeFirst() throws InterruptedException {
        LinkedTransferQueue<Integer> transferQueue=new LinkedTransferQueue();
        Integer take = transferQueue.take();
        System.out.println("take "+take);
        transferQueue.add(1);
        System.out.println(transferQueue.size());
    }

    private static void addFirst() throws InterruptedException {
        LinkedTransferQueue<Integer> transferQueue=new LinkedTransferQueue();
        transferQueue.add(1);
        Integer take = transferQueue.take();
        System.out.println(transferQueue.size());
    }

    private static void doubleAdd() throws InterruptedException {
        LinkedTransferQueue<Integer> transferQueue=new LinkedTransferQueue();
        transferQueue.add(1);
        transferQueue.add(2);
        transferQueue.add(3);
        transferQueue.add(4);
//        Integer take = transferQueue.take();
//        Integer take2 = transferQueue.take();
        System.out.println(transferQueue.size());

        LinkedBlockingQueue<Integer> blockingQueue=new LinkedBlockingQueue();
        blockingQueue.add(1);
        blockingQueue.add(2);
        Integer peek = blockingQueue.peek();
        System.out.println(peek);
    }

}
