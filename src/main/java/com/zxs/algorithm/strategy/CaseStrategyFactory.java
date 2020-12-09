package com.zxs.algorithm.strategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CaseStrategyFactory {

	public static <T> CashStargety createCashStrategy(String className) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> claz=Class.forName(className);
		Constructor<CashStargety> constructor=(Constructor<CashStargety>) claz.getConstructor();
		return constructor.newInstance();
	}
	
	public static <T> CashStargety createCashStrategy(int flag) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		 switch (flag) {
		case 1:
			return new DiscountCashStrategy();
		case 2:
			return new FullReductionCashStrategy();
		}
		return null;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		CashStargety caStargety=CaseStrategyFactory.createCashStrategy("com.zxs.algorithm.strategy.DiscountCashStrategy");
		System.out.println(caStargety);
	}
}
 