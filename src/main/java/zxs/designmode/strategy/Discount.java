package zxs.designmode.strategy;

public interface Discount {
    /**
     * 打折接口
     * @param amount
     * @param discountoff
     * @param type:0打折百分百，1满减
     * @return
     */
	public double discout(double amount,double discountoff,int type);
	
}
