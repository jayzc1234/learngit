package zxs.designmode.adapter;

public class WildCock implements Cock{

	@Override
	public void fly() {
      System.out.println("cock fly");		
	}

	@Override
	public void gobble() {
		System.out.println("cock gobble");		
	}

}
