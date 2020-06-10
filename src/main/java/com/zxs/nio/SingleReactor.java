package com.zxs.nio;

import com.zxs.pojo.Person;
import com.zxs.pojo.Student;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import ognl.Ognl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * @author zc
 */
public class SingleReactor {

    private ServerSocketChannel serverSocketChannel;
    private static int port =9901;
    private static String host="localhost";

    public SingleReactor() throws IOException {
        this.serverSocketChannel =ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        serverSocketChannel.bind(inetSocketAddress);
    }

    public static void main(String[] args) throws Exception {
//        SingleReactor singleReactor=new SingleReactor();
//        Student student=new Student();
//        student.setStNo("88");
//        Selector selector = Selector.open();
//        selector.wakeup();
//
//        SelectionKey selectionKey=singleReactor.serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT,student);
//        selectionKey.attach(student);

// 创建两个 EventLoopGroup 对象
//        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 创建 boss 线程组 用于服务端接受客户端的连接
//        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 创建 worker 线程组 用于进行 SocketChannel 的数据读写
//// 创建 ServerBootstrap 对象
//        ServerBootstrap b = new ServerBootstrap();
//// 设置使用的 EventLoopGroup
//        ServerBootstrap group = b.group(bossGroup, workerGroup);
        run();
    }

    public static void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
//                                ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
