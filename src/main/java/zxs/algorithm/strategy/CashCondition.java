package zxs.algorithm.strategy;

public class CashCondition {
	private int flag;

	private double discount;

	private double standardMoney;

	private double reduceMoney;

	public CashCondition(int flag, double discount) {
		super();
		this.flag = flag;
		this.discount = discount;
	}

	public CashCondition(int flag, double standardMoney, double reduceMoney) {
		super();
		this.flag = flag;
		this.standardMoney = standardMoney;
		this.reduceMoney = reduceMoney;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public double getStandardMoney() {
		return standardMoney;
	}

	public void setStandardMoney(double standardMoney) {
		this.standardMoney = standardMoney;
	}

	public double getReduceMoney() {
		return reduceMoney;
	}

	public void setReduceMoney(double reduceMoney) {
		this.reduceMoney = reduceMoney;
	}

}
