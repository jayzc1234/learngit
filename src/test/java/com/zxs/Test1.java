package com.zxs;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Test1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        int i = 1 << 2;
        System.out.println(i);

    }

    private static void test() {
        try {
            byte[] array1 = new byte[1000 * 1024];
            array1 = null;
            array1 = new byte[ 200 * 1024];
            array1 = null;
            array1 = new byte[ 200 * 1024];

            byte[] arrayo = new byte[5 * 1024 * 1024];
            arrayo = null;
            byte[] array1q = new byte[1 * 1024 * 1024];
            byte[]array1a = new byte[2 * 1024 * 1024];
            byte[] arrayb = new byte[2 * 1024 * 1024];
            byte[] arrayc = new byte[2 * 1024 * 1024];

            byte[] arraya = new byte[900* 1024];
            array1 =null;

            array1 = new byte[200 * 1024];
            array1 = null;

            array1 = new byte[5 * 1024 * 1024];
            byte[] array11 = new byte[512 * 1024];
            byte[] array111 = new byte[512 * 1024];
            byte[] array12 = new byte[160 * 1024];
            array1 = new byte[1 * 1024 * 1024];
            array1 = new byte[100 * 1024 * 1024];
            array1 = null;
            array1 = new byte[2 * 1024 * 1024];

        }catch (Throwable throwable){
            System.out.println("throwable 拉");
        }
        finally {
            System.out.println("oom 拉");
        }
    }
}
