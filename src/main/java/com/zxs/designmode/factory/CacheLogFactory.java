package com.zxs.designmode.factory;

import com.zxs.designmode.factory.handlers.CachedLogHandler;
import com.zxs.designmode.factory.handlers.LogHandler;

public class CacheLogFactory implements LogFactory{

	@Override
	public LogHandler createLogHander(String name) {
		
		return new CachedLogHandler();
	}

}
