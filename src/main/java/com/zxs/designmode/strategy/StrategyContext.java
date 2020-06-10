package com.zxs.designmode.strategy;

public class StrategyContext {
	private Discount discount;

	public Discount getDiscount() {
		return discount;
	}

	public void setDiscount(Discount discount) {
		this.discount = discount;
	}

	public StrategyContext(Discount discount) {
		this.discount = discount;
	}

	public double discount(double amount, double discountoff, int type) {
		return discount.discout(amount, discountoff, type);
	}
}
