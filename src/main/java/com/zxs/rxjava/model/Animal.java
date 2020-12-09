package com.zxs.rxjava.model;

import lombok.Data;

@Data
public class Animal {
    protected String name= "Animal";

    public Animal(){
        System.out.println("create animal "+name);
    }
}
