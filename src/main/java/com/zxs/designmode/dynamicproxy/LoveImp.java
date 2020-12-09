package com.zxs.designmode.dynamicproxy;

public class LoveImp implements LoveInterface{

	@Override
	public void whoisLover(String name) {
		System.out.println("执行whoisLover:"+getName(name));
	}

	@Override
	public String getName(String name) {
		System.out.println("执行getName");
		return name;
	}

}
