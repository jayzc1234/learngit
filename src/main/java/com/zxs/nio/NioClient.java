package com.zxs.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;

public class NioClient {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(HOST, PORT));
        boolean b = socketChannel.finishConnect();
        if (b){
            socketChannel.register(selector,SelectionKey.OP_WRITE);
            while (true){
                int select = selector.select();
                if (select > 0){
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    for (SelectionKey selectionKey : selectionKeys) {
                        if (selectionKey.isWritable()){
                            SocketChannel channel = (SocketChannel) selectionKey.channel();
                            channel.configureBlocking(false);
                            ByteBuffer byteBuffer = ByteBuffer.allocate(12);
                            byteBuffer.put("hello server".getBytes());
                            byteBuffer.flip();
                            if (byteBuffer.hasRemaining()){
                                channel.write(byteBuffer);
                            }
                            byteBuffer.compact();
                        }
                    }
                    selectionKeys.clear();
                }
            }
        }
    }
}
