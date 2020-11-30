package zxs.designmode.strategy;

public class CashDiscount implements Discount{

	@Override
	public double discout(double amount, double discountoff, int type) {
		if (type!=1) {
			throw new RuntimeException("type类型必须为1");
		}
		return amount-discountoff;
	}

}
