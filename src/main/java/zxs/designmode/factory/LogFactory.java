package zxs.designmode.factory;

import zxs.designmode.factory.handlers.LogHandler;

public interface LogFactory {
 LogHandler createLogHander(String name);
}
