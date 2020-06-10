package com.zxs.designmode.adapter;

public class CockAdapter implements Duck{
    private Cock cock;
    
	public CockAdapter(Cock cock) {
		this.cock = cock;
	}

	public Cock getCock() {
		return cock;
	}

	public void setCock(Cock cock) {
		this.cock = cock;
	}

	@Override
	public void fly() {
		System.out.println("CockAdapter's duck fly");
	}

	@Override
	public void quack() {
		System.out.println("CockAdapter's duck quack");
	}

}
