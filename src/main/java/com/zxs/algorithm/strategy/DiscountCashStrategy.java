package com.zxs.algorithm.strategy;

public class DiscountCashStrategy implements CashStargety{

	private double discout;
	
	public DiscountCashStrategy(double discout) {
		this.discout = discout;
	}

	@Override
	public double calculate(double money) {
		return money*(discout/10);
	}

	public DiscountCashStrategy() {
	}

}
