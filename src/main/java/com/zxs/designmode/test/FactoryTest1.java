package com.zxs.designmode.test;

import com.zxs.designmode.factory.FileLogFactory;
import com.zxs.designmode.factory.LogFactory;
import com.zxs.designmode.factory.handlers.LogHandler;

public class FactoryTest1 {
  public static void main(String[] args) {
	LogFactory factory=new FileLogFactory();
	
	LogHandler hander=factory.createLogHander(null);
	hander.saveLog("sss");
  }
}
