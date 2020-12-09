package com.zxs.designmode.factory;

import com.zxs.designmode.factory.handlers.FileLogHandler;
import com.zxs.designmode.factory.handlers.LogHandler;

public class FileLogFactory implements LogFactory{

	@Override
	public LogHandler createLogHander(String name) {
		return new FileLogHandler();
	}


}
