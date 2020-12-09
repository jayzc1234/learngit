package com.zxs.designmode.factory.handlers;

public class FileLogHandler implements LogHandler{

	@Override
	public void saveLog(String log) {
       System.out.println("日志"+log+"存入文件");		
	}

}
