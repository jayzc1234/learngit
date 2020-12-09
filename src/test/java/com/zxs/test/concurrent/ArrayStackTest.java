package com.zxs.test.concurrent;

import java.util.Stack;

public class ArrayStackTest<T> {

    private Object[] elementData;

    /**
     * 标记当前元素所在位置
     */
    private int index;

    /**
     * 标记当前栈的长度
     */
    private int length;

    private static final int DEFAULT_SIZE=50;

    public ArrayStackTest(int size){
        elementData =new Object[size];
    }

    public ArrayStackTest(){
        elementData =new Object[DEFAULT_SIZE];
    }

    public boolean push (T t){
        elementData[index++]=t;
        return true;
    }

    public T pop (){
        return (T) elementData[--index];
    }

    public int size(){
        return index;
    }
    public static void main(String[] args) {
        Stack<Integer> stack=new Stack();
        stack.push(1);
        stack.push(2);
        Integer pop2 = stack.pop();
        Integer pop3 = stack.pop();
        System.out.println(pop2);


        ArrayStackTest<Integer> arrayStackTest=new ArrayStackTest<>();
        arrayStackTest.push(1);
        arrayStackTest.push(2);
        int size = arrayStackTest.size();
        System.out.println(size);
        System.out.println("------------------------------------------------------");
        Integer pop = arrayStackTest.pop();
        Integer pop1 = arrayStackTest.pop();
        System.out.println(pop);
        System.out.println(pop1);
    }
}
