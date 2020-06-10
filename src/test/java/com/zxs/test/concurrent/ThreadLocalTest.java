package com.zxs.test.concurrent;

import com.zxs.pojo.Person;
import io.swagger.models.auth.In;

import java.io.IOException;

public class ThreadLocalTest {

    public static void main(String[] args) throws IOException, InterruptedException {

        Person person=new Person();
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(person.getId());
            }
        });
        thread.start();
        Thread.sleep(100);

        person.setId(12);
        System.in.read();
    }

    private static void test1() throws IOException {
        InheritableThreadLocal<Long> inheritableThreadLocal=new InheritableThreadLocal<>();
        inheritableThreadLocal.set(2L);

        ThreadLocal<Long> threadLocal=new ThreadLocal<>();
        threadLocal.set(1L);

        Person person=new Person();
        InheritableThreadLocal<Person> personInheritableThreadLocal=new InheritableThreadLocal<>();
        personInheritableThreadLocal.set(person);

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Person person1 = personInheritableThreadLocal.get();
                System.out.println(person1.getId());
            }
        });
        thread.start();
        person.setId(12);
        System.out.println("主线程结束");
        System.in.read();
    }
}
