package zxs.test.innerclass;

public class Outer {
     int outAge;
    
	class Inner{
		private int age;

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
		
		public Outer setAge() {
			return Outer.this;
		}
		
	}
	
	static class Inner2{
		
	}
	
	public Inner  returnInner() {
		return new Inner();
	}
	public static void main(String[] args) {
		Outer o=new Outer();
		Inner in=o.new Inner();
		Inner2 in2=new Inner2();
		if (in2 instanceof Inner2) {
		}
		System.out.println(in.age);
	}
}
