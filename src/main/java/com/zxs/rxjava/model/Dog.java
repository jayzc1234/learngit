package com.zxs.rxjava.model;

import lombok.Data;

@Data
public class Dog extends Animal {

    public Dog(){
        name=getClass().getSimpleName();
        System.out.println("create "+name);
    }
}
