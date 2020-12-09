package com.zxs.nio.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 多reactor测试
 * @author zc
 */
public class NioServerMultiReactor {
    /**
     * 思路：selector.open()多个selector，一个selector负责监听serverSocketChannel接收新的连接，
     * 另一个则对新的客户端连接的socketChannel进行监听处理新事件
     */

    private static int port=9987;

    private static String hostName="localhost";

    private static volatile ServerSocketChannel serverSocketChannel;

    private static ExecutorService executorService;
    private static  Selector selector;
    private  static Selector secondSelector;

    static {

        executorService=new ThreadPoolExecutor(50,50,20L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(6));
        try {
            selector =Selector.open();
            secondSelector=Selector.open();
            if (serverSocketChannel==null){
                synchronized (NioServerMultiReactor.class){
                    if (serverSocketChannel==null){
                        serverSocketChannel=ServerSocketChannel.open();
                        serverSocketChannel.configureBlocking(false);
                        serverSocketChannel.bind(new InetSocketAddress(port));
                    }
                }
            }
//            serverSocketChannel.bind(new InetSocketAddress(hostName,port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    void accept() throws IOException {
        System.out.println("服务端启动接收连接");
        while(true){
            int select = selector.select();
            if (select>0){
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey next = iterator.next();
                    boolean acceptable = next.isAcceptable();
                    if (acceptable){
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        if (null!=socketChannel){
                            System.out.println("获取新连接："+socketChannel.getClass());
                            socketChannel.configureBlocking(false);
                            executorService.submit(new SocketDepart(socketChannel));
                        }
                    }else if (next.isReadable()){
                        System.out.println("服务端可读事件");
                    }else if (next.isWritable()){
                        System.out.println("服务端可写事件");
                    }
                    iterator.remove();
                }
            }
        }
    }

    class SocketDepart implements  Runnable{
        private SocketChannel socketChannel;
        public SocketDepart(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }
        @Override
        public void run() {
            try {
                SelectionKey register = socketChannel.register(secondSelector, SelectionKey.OP_READ);
                int select = secondSelector.select();
                if (select>0){
                    Set<SelectionKey> selectionKeys = secondSelector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey next = iterator.next();
                        boolean acceptable = next.isAcceptable();
                        int i = next.interestOps();
                        iterator.remove();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NioServerMultiReactor nioServerMultiReactor=new NioServerMultiReactor();
        nioServerMultiReactor.accept();
//        SelectionKey selectionKey = NioServerMultiReactor.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
//        Set<SelectionKey> selectionKeys = selector.selectedKeys();
//        int i = selectionKey.interestOps();
//        System.out.println(i);
//
//        selectionKey.interestOps(SelectionKey.OP_READ);
//         i = selectionKey.interestOps();
//        System.out.println(i);
    }
}
