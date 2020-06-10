package com.zxs.algorithm.strategy;

public class CashStragtegyMain {

	public static void main(String[] args) {
 		CashContext cashContext=new CashContext(new CashCondition(1, 8));
 		double m=cashContext.calculate(50);
 		System.out.println(m);
	}
	
	
}
