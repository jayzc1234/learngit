package com.zxs.test.generics;

import java.util.Iterator;
import java.util.function.Consumer;

import com.zxs.pojo.Student;

public class Coffee extends Student implements Impossible{
	private static long counter = 0;
	private final long id = counter++;
	public String toString() {
	return getClass().getSimpleName() + " " + id;
	}
	
	public void Iterabled() {
//		super.
		String name=Coffee.super.getClass().getName();
		System.out.println(name);
	}
	
	public static void main(String[] args) {
		Coffee c=new Coffee();
		c.Iterabled();
	}
}
