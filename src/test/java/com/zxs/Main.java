package com.zxs;

import com.zxs.pojo.Person;
import com.zxs.pojo.Person2;
import com.zxs.pojo.Student;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

public class Main  {
	private Person person;
	private Student student;
	private static final int COUNT_BITS = Integer.SIZE - 3;
	private static final int CAPACITY   = (1 << COUNT_BITS) - 1;
	private static final int CAPACITY2   = (1 << COUNT_BITS);
	// runState is stored in the high-order bits
	private static final int RUNNING    = -1 << COUNT_BITS;
	private static final int SHUTDOWN   =  0 << COUNT_BITS;
	private static final int STOP       =  1 << COUNT_BITS;
	private static final int TIDYING    =  2 << COUNT_BITS;
	private static final int TERMINATED =  3 << COUNT_BITS;
	public Main() {
		Person person=new Person();
		Student student=new Student();
		this.person=person;
		this.student=student;
	}

	public static void main(String[] args) throws Exception {
        List<Ordered> list = new ArrayList<>();
		Person2 person2 = new Person2();
		Person person = new Person();
		list.add(person2);
		list.add(person);
		AnnotationAwareOrderComparator in = new AnnotationAwareOrderComparator();
		list.sort(in);
		for (Ordered ordered : list) {
			System.out.println(ordered);
		}

//       for (;;){
//		   FieldBusinessParam fieldBusinessParam = new FieldBusinessParam();
//		   list.add(fieldBusinessParam);
//	   }
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