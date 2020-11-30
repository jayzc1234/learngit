package zxs.designmode.factory;

import zxs.designmode.factory.handlers.LogHandler;
import zxs.designmode.factory.handlers.FileLogHandler;

public class FileLogFactory implements LogFactory{

	@Override
	public LogHandler createLogHander(String name) {
		return new FileLogHandler();
	}


}
