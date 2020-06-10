package com.zxs.designmode.adapter;

public class WildDuck implements Duck{

	@Override
	public void fly() {
       System.out.println("duck fly");		
	}

	@Override
	public void quack() {
		System.out.println("duck quack");				
	}

}
