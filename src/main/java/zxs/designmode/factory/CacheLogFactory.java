package zxs.designmode.factory;

import zxs.designmode.factory.handlers.CachedLogHandler;
import zxs.designmode.factory.handlers.LogHandler;

public class CacheLogFactory implements LogFactory{

	@Override
	public LogHandler createLogHander(String name) {
		
		return new CachedLogHandler();
	}

}
