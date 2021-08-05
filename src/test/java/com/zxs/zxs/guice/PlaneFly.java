package com.zxs.zxs.guice;

public class PlaneFly implements Fly {
    @Override
    public void fly(String where) {
        System.out.println("fly to air");
    }
}
