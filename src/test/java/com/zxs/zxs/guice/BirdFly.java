package com.zxs.zxs.guice;

public class BirdFly implements Fly {
    @Override
    public void fly(String where) {
        System.out.println("fly to ground");
    }
}
