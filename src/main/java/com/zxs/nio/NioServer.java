package com.zxs.nio;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.ibatis.annotations.SelectKey;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;

public class NioServer {
    private static final int PORT = 9999;
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        /**
         * 处理事件
         */
        while (true){
            System.out.println("服务端进入阻塞等待客户端连接");
            int select1 = selector.select();
            if (select1>0){
                Set<SelectionKey> select = selector.selectedKeys();
                for (SelectionKey selectionKey : select) {
                    if (selectionKey.isAcceptable()){
                        SocketChannel accept = serverSocketChannel.accept();
                        accept.configureBlocking(false);
                        accept.register(selector, SelectionKey.OP_READ);
                    }else if (selectionKey.isReadable()){
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        channel.read(byteBuffer);
                        byteBuffer.flip();
                        byte[] array = byteBuffer.array();
                        System.out.println("接收到客户端消息："+new String(array));
//                        selectionKey.cancel();
                        channel.register(selector, SelectionKey.OP_WRITE);
                    }else if (selectionKey.isWritable()){
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(12);
                        byteBuffer.put("hello client".getBytes());
                        channel.write(byteBuffer);
                        selectionKey.cancel();
                    }
                }
                select.clear();
            }
            break;
        }
    }
}
