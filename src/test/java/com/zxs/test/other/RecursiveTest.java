package com.zxs.test.other;

public class RecursiveTest {
    public static void main(String[] args) {
        char[] chars=new char[]{'a','b','c','d','e'};
//        test1(0,chars.length-1,chars);
//        for (char aChar : chars) {
//            System.out.println(aChar);
//        }
        test2(chars);
        for (char aChar : chars) {
            System.out.println(aChar);
        }
    }

    public static void test1(int min,int max,char[] chars) {
        if (min<max){
            char aChar = chars[min];
            chars[min]=chars[max];
            chars[max]=aChar;
            test1(min+1,max-1,chars);
        }
    }

    public static void test2(char[] chars) {
      int left=0,right=chars.length-1;
      while (left<right){
          char aChar = chars[left];
          chars[left]=chars[right];
          chars[right]=aChar;
          left++;
          right--;
      }
    }
}
