package zxs.algorithm.strategy;

public class FullReductionCashStrategy implements CashStargety{

	private double standardMoney;
	private double reduce;
	
	@Override
	public double calculate(double money) {
		if (money>=standardMoney) {
			return money-reduce;
		}
		return reduce;
	}

	public FullReductionCashStrategy(double standardMoney, double reduce) {
		this.standardMoney = standardMoney;
		this.reduce = reduce;
	}

	public FullReductionCashStrategy() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
