package com.zxs.test.suiyi;

public abstract class AbstractPInterface1 implements PInterface1 {
    @Override
    public void print(String name) {
        System.out.println("AbstractPInterface1:"+name);
    }
}
