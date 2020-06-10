package com.zxs.designmode.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyInvokeHander implements InvocationHandler{

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("动态代理执行："+method.getName()+"方法");
		return method.invoke(proxy, args);
	}

}
