package com.zxs.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.zxs.pojo.Person;

public class MainTest implements InvocationHandler {
	
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<T> clz) {
		return (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class[] { clz }, this);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			try {
				// 诸如hashCode()、toString()、equals()等方法，将target指向当前对象this
				return method.invoke(this, args);
			} catch (Throwable t) {
			}
		}
		return null;
	}
  public static String DEFAULT_CHARSET="UTF-8";
  private static boolean initialized = false;

  static {
//      Thread t = new Thread(() -> initialized = true);
//      t.start();
//      try {
//          t.join();
//      } catch (InterruptedException e) {
//          throw new AssertionError(e);
//      }
	  System.out.println("执行静态代码块");
  }

  public static void main(String[] args) {
//      System.out.println(initialized);
      MainTest m=new MainTest();
      System.out.println(m.test());
  }
    int a;
	public int test() {
		
		try {
			a = 1;
			return a;
		} catch (Exception e) {
			a = 2;
			return a;
		} finally {
			a = 3;
			System.out.println("执行啦，a="+a);
		}
	}
	public Person test2() {
		Person a=null;
		try {
			a = new Person();
			a.setId(1);
			return a;
		} catch (Exception e) {
			a = new Person();
			a.setId(2);
			return a;
		} finally {
			a = new Person();
			a.setId(3);
			System.out.println("执行啦，a="+a);
		}
	}
}
