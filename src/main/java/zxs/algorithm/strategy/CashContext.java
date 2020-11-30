package zxs.algorithm.strategy;

public class CashContext {
  private CashStargety cashStargety;

  private CashCondition cashCondition;
  
	public CashContext(CashCondition cashCondition) {
		this.cashCondition=cashCondition;
		int flag=cashCondition.getFlag();
		switch (flag) {
		case 1:
			this.cashStargety=new DiscountCashStrategy(cashCondition.getDiscount());
			break;
		case 2:
			this.cashStargety=new FullReductionCashStrategy(cashCondition.getStandardMoney(), cashCondition.getReduceMoney());
			break;
		default:
			break;
		}
	}
  
	public double calculate(double money) {

		return cashStargety.calculate(money);
	}
  
	
	
}
