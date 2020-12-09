package com.zxs.test.concurrent;

import lombok.Data;

import java.math.BigDecimal;

public class LinkedStack<T> {
    @Data
    private class Node{
        Object item;
        Node next;

        public Node(Object item){
            this.item=item;
        }
    }

    private Node top;


    public boolean push(T t){
        Node node=new Node(t);
        if (top==null){
            top=node;
        }else {
            top.next=node;
            top=node;
        }
        return  true;
    }

    public static void main(String[] args) {


    }
}
