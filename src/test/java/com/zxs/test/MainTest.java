package com.zxs.test;

import java.util.ArrayList;

public class MainTest  {

    public static void main(String[] args)throws Exception {
        String str1 = "aaa";

        String str2 = "bbb";

        String str3 = "aaabbb";

        String str4 = str1 + str2;

        String str5 = "aaa" + "bbb";

        System.out.println(str3 == str4.intern()); // true or false
        System.out.println(str3 == str5);// true or false



        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(1);
        arrayList.ensureCapacity(1);
        Integer integer = arrayList.get(0);
        int i = Integer.MAX_VALUE + 1;
        System.out.println(i);
    }

}