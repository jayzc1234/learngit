package com.zxs.test.jvm;

import java.util.*;

public class HeapOOM {

    public void tt(){

    }
    public static class OOMObject{

        static String name;
        public static void setName(String name) {
            OOMObject.name = name;
        }

        public static void  add(){
            List<OOMObject> list=new ArrayList<OOMObject>();
            Random random=new Random();
            while (true){
                OOMObject oomObject=new OOMObject();
                name="sss"+random.nextInt(1000000);
                oomObject.setName(name);
                list.add(oomObject);
            }
        }
    }

    public static void main(String[] args) {
        while (true){
            HeapOOM.OOMObject.add();
        }
    }
}
