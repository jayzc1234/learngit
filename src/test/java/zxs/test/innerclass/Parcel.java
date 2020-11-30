package zxs.test.innerclass;

import java.util.Iterator;

import zxs.pojo.Person;
import zxs.test.interfce.BasePrint;

public class Parcel {
    private int age;
	protected class RealPrint extends Person implements BasePrint,Iterable<Integer>{
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
			System.out.println("Parcel:RealPrint");
		}
	}
	
	
	public Parcel() {
		System.out.println("Parcel");
		new RealPrint();
	}
	public BasePrint getPrint() {
		return new RealPrint();
	}
	
	public BasePrint getPrint2() {
		return new BasePrint() {
			@Override
			public void print(String name) {
				System.out.println(age);
			}
		};
	}
	
	public static void main(String[] args) {
		Parcel p=new Parcel();
		BasePrint basePrint=p.getPrint();
		basePrint.print("inner class");
	}
}
