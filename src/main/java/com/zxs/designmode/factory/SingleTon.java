package com.zxs.designmode.factory;

public class SingleTon {
  private static class Hodler{
	  private static SingleTon instance=new SingleTon();
  }
  public static SingleTon getInstance() {
	  return Hodler.instance;
  }
  private SingleTon() {
	  System.out.println("初始化SingleTon");
  }
  public static void test() {
	  System.out.println("test");
  }
  public static void main(String[] args) {
	SingleTon.test();
	
	
  }
}
