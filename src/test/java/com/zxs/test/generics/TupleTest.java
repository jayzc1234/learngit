package com.zxs.test.generics;

import com.zxs.pojo.Person;
import com.zxs.pojo.Student;

public class TupleTest {
	static TwoTuple<String, Integer> f() {
		// Autoboxing converts the int to Integer:
		return new TwoTuple<String, Integer>("hi", 47);
	}

	static ThreeTuple<Amphibian, String, Integer> g() {
		return new ThreeTuple<Amphibian, String, Integer>(new Amphibian(), "hi", 47);
	}

	static FourTuple<Vehicle, Amphibian, String, Integer> h() {
		return new FourTuple<Vehicle, Amphibian, String, Integer>(new Vehicle(), new Amphibian(), "hi", 47);
	}

	static FiveTuple<Vehicle, Amphibian, String, Integer, Double> k() {
		return new FiveTuple<Vehicle, Amphibian, String, Integer, Double>(new Vehicle(), new Amphibian(), "hi", 47,
				11.1);
	}

	static FourTuple<String, Integer, Person, Student> g2(){
		return new FourTuple<String, Integer, Person, Student>("ddd", 2, new Person(), new Student());
	}
	public static void main(String[] args) {
		TwoTuple<String, Integer> ttsi = f();
		System.out.println(ttsi);
		// ttsi.first = "there"; // Compile error: final
		System.out.println(g2());
		System.out.println(h());
		System.out.println(k());
	}
}

class Amphibian {
}

class Vehicle {
}