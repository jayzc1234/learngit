package com.zxs.designmode.factory;

import com.zxs.designmode.factory.handlers.LogHandler;

public interface LogFactory {
 LogHandler createLogHander(String name);
}
