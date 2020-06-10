package com.zxs.pojo;

public class Student extends Person{

	public Student(String stNo){
		this.stNo=stNo;
	}
	public Student(){
	}
	private String stNo;

	public String getStNo() {
		return stNo;
	}

	public void setStNo(String stNo) {
		this.stNo = stNo;
	}

	public static void main(String[] args) {
		Student student=new Student("学生1");
	}
}
