package com.zxs.test.other;

import java.util.Iterator;

import com.zxs.pojo.Person;
import com.zxs.test.innerclass.Parcel;
import com.zxs.test.interfce.BasePrint;

public class Test1 extends Parcel{
	protected class RealPrint extends Person  implements BasePrint,Iterable<Integer>{
		 private int age2;
		@Override
		public void print(String name) {
			System.out.println("RealPrint:"+name);
		}
		@Override
		public Iterator<Integer> iterator() {
			// TODO Auto-generated method stub
			return null;
		}
		public RealPrint() {
			System.out.println("Test1:RealPrint");
		}
	}
  
  public Test1() {
	  new RealPrint();
	}

public static void main(String[] args) {
	  Test1 t1=new Test1();
  }
}
