package zxs.designmode.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Serialize implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	private int age;
	
  public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
	  Serialize s=new Serialize();
	  s.setAge(12);
	  s.setName("jay");
	  
	  ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream("f://user.obj"));
	  oos.writeObject(s);
	  oos.flush();
	  
	  
	  ObjectInputStream ois=new ObjectInputStream(new FileInputStream("f://user.obj"));
	  Serialize s2=(Serialize) ois.readObject();
	  s2.setName("chou");
	  System.out.println(s2.getName());
}
}
