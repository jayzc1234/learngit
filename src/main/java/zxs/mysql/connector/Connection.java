package zxs.mysql.connector;



import zxs.mysql.protocol.ServerProtocolHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * mysql连接,一个连接就是一个socket
 * @author zc
 */
public class Connection {

    private static final String HOST = "192.168.2.215";

    private static final int PORT = 3306;

    private static Selector selector;

    public static final String DB = "test";
    public static final String USERNAME = "jgw";
    public static final String PWD = "Jgw*31500-2018.6";

    static {
        try {
            selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            boolean connect = socketChannel.connect(new InetSocketAddress(HOST, PORT));
            if (!connect){
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public  void handleEvent() throws IOException {
        while (true){
            int select = selector.select();
            if (select > 0){
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    channel.configureBlocking(false);
                    if (selectionKey.isConnectable()){
                        boolean b = channel.finishConnect();
                        if (b){
                            channel.register(selector,SelectionKey.OP_READ);
                        }
                    }else if (selectionKey.isReadable()){
                        doRead(channel);
                    }else if (selectionKey.isWritable()){
                        doWrite(channel);
                    }
                    iterator.remove();
                }
            }
        }
    }

    private void doWrite(SocketChannel channel) {
    }

    private void doRead(SocketChannel channel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int len = 0;
        while ((len =channel.read(byteBuffer)) != 0){
            System.out.println("开始读取MySQL server返回的数据，当前读取长度："+len);
           if (len == -1){
               channel.close();
               break;
           }

           if (byteBuffer.remaining() < 1){
               //如果接收的字节数已经超过了byteBuffer的长度则对byte buffer扩展。
               byteBuffer = expandBuf(byteBuffer);
           }
        }
        //准备读取数据
        ServerProtocolHandler.handleServerPacket(byteBuffer);
    }

    /**
     * 对byte buffer进行扩容
     * @param byteBuffer
     * @return
     */
    private ByteBuffer expandBuf(ByteBuffer byteBuffer) {
        byteBuffer.flip();
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(byteBuffer.capacity() * 2);
        byteBuffer1.put(byteBuffer);
        return byteBuffer1;
    }

    public Connection getConnection(){



        return this;
    }

    public static void main(String[] args) throws IOException {

        int data = 10;
        String s = Integer.toBinaryString(data);
        System.out.println(s);
        Connection connection = new Connection();
        connection.handleEvent();
    }
}
