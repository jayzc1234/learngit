package com.zxs;

import com.zxs.pojo.Person;
import com.zxs.pojo.Student;
import org.springframework.aop.framework.ProxyFactory;

import com.zxs.designmode.dynamicproxy.advice.MyAdvice;
import com.zxs.designmode.dynamicproxy.advisor.Myadvisor;
import com.zxs.test.interfce.BasePrint;
import com.zxs.test.interfce.BasePrintImp;

import java.lang.reflect.Field;
import java.util.List;

public class Main extends ApplicationBootApp {
	private Person person;
	private Student student;

	public Main() {
		Person person=new Person();
		Student student=new Student();
		this.person=person;
		this.student=student;
	}

	public static void main(String[] args) {
	   Main main=new Main();
	   tt t=main.new tt();
	   Person person1 = main.getPerson();
	   Class<?> declaringClass = t.getClass().getDeclaringClass();
	   Class<?>[] declaredClasses = Main.class.getDeclaredClasses();
	   Class<?>[] declaredClasses1 = main.getClass().getClasses();

		Field[] declaredFields = main.getClass().getDeclaredFields();
		for (Field declaredField : declaredFields) {
			Class<?> declaringClass1 = declaredField.getDeclaringClass();
			System.out.println(declaredClasses1);
		}

		for (Class<?> declaredClass : declaredClasses) {
		   System.out.println(declaredClass);
	   }
   }

	private static void test1() {
		ProxyFactory proxyFactory=new ProxyFactory();
		proxyFactory.setInterfaces(BasePrint.class);
		MyAdvice advice=new MyAdvice();
		proxyFactory.addAdvice(advice);
		BasePrintImp basePrintImp=new BasePrintImp();
		proxyFactory.setTarget(basePrintImp);
		Myadvisor myadvisor=new Myadvisor(advice);
		proxyFactory.addAdvisor(myadvisor);
		BasePrint obj=(BasePrint) proxyFactory.getProxy();
		obj.print("ss");
		System.out.println(obj);
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	class tt{

	}
}