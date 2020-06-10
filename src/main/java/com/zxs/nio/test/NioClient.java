package com.zxs.nio.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * nio客户端
 * @author zc
 */
public class NioClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel=SocketChannel.open(new InetSocketAddress("localhost",9987));
        socketChannel.configureBlocking(false);
        Selector selector= Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT|SelectionKey.OP_READ);
        while (true){
            int select = selector.select();
            System.out.println("轮询获取事件");
            if (select>0){
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()){
                    SelectionKey next = iterator.next();
                    next.cancel();
                    if (next.isConnectable()){
                        socketChannel.register(selector,SelectionKey.OP_READ);
                    }else if (next.isReadable()){
                        System.out.println("客户端：可读事件发生");
                    }else if (next.isWritable()){
                        System.out.println("客户端：可写事件发生");
                    }
                    iterator.remove();
                }
            }
        }
    }
}
