package com.zxs.zxs.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceTest1 {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new BasicModule());
        Fly instance = injector.getInstance(Fly.class);
        instance.fly("a");

    }
}
