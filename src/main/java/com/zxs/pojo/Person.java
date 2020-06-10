package com.zxs.pojo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class Person {
  private int id;
  
  private String name;

  public Person(){
      System.out.println("执行person构造器");
  }

  public int getId() {
        System.out.println("执行person getId方法");
	return id;
    }

    public void setId(int id) {
            System.out.println("执行person getId方法");
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

       public abstract class  Personhandler{

       }
}
