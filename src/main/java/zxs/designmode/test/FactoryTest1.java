package zxs.designmode.test;

import zxs.designmode.factory.FileLogFactory;
import zxs.designmode.factory.LogFactory;
import zxs.designmode.factory.handlers.LogHandler;

public class FactoryTest1 {
  public static void main(String[] args) {
	LogFactory factory=new FileLogFactory();
	
	LogHandler hander=factory.createLogHander(null);
	hander.saveLog("sss");
  }
}
