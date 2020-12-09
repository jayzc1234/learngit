package com.zxs.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Hold<T> {
 private T h;
 private List<T> list;

public Hold(T h) {
	super();
	this.h = h;
	this.list=new ArrayList<T>();
}

public  void add(T t) {
	list.add(t);
}

public  void print() {
	System.out.println(h.toString());
}

public  T retu() {
	return h;
}

public static void main(String[] args) throws ParseException, IOException, InterruptedException {
	ByteBuffer buffer=ByteBuffer.allocate(5);
	ServerSocketChannel channel=ServerSocketChannel.open();
	channel.configureBlocking(false);
	channel.bind(new InetSocketAddress(88));
	Thread t=new Thread();
	t.join();
	while(true) {
		SocketChannel socketChannel=channel.accept();
		System.out.println(socketChannel);
	}
 }
}
