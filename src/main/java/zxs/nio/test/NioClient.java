package zxs.nio.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
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
        SocketChannel socketChannel=SocketChannel.open();
        socketChannel.configureBlocking(false);
        Selector selector= Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        while (true){
            int select = selector.select();
            System.out.println("轮询获取事件");
            if (select>0){
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()){
                    SelectionKey next = iterator.next();
                    if (next.isConnectable()){
                        SocketChannel channel = (SocketChannel) next.channel();
                        channel.connect(new InetSocketAddress(9987));
                        boolean b = channel.finishConnect();
                        if (b){
                            socketChannel.register(selector,SelectionKey.OP_READ);
                        }
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
