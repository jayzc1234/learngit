package com.zxs.test.concurrent;

import java.security.PublicKey;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class CopyOnWriteArrayListTest {
     long c;
     boolean flag;
    CopyOnWriteArrayList<Integer> copyOnWriteArrayList;

    public  void  write(long c){
     this.c=c;  //操作1
     this.flag=true; //操作2
        copyOnWriteArrayList.set(1,1); //操作3  由写volatile的happens-before原则可知，对其前后的代码都禁止重排序
    }

    public  void  read(long c){
        copyOnWriteArrayList.get(1); //操作4
        if (flag){  //操作5
            long i=c*c;  //操作6
        }
    }

    public static void main(String[] args) {

        int a=1;
        int b=2;

        CopyOnWriteArrayList<Integer> copyOnWriteArrayList=new CopyOnWriteArrayList();
        copyOnWriteArrayList.set(1,1);
        for (Integer integer : copyOnWriteArrayList) {

        }
        Iterator<Integer> iterator = copyOnWriteArrayList.iterator();

    }
}
