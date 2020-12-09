package com.zxs.designmode.dynamicproxy;

import java.lang.reflect.Proxy;

public class DynamicMain {
 public static void main(String[] args) {
	 LoveImp proxy=(LoveImp) Proxy.newProxyInstance(LoveImp.class.getClassLoader(), new Class[] {LoveInterface.class}, new MyInvokeHander());
	 proxy.whoisLover("fjl");
	
 }
}
