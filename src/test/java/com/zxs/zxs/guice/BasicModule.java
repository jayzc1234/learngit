package com.zxs.zxs.guice;

import com.google.inject.AbstractModule;

public class BasicModule extends AbstractModule {

    @Override
    protected void configure() {
        // 表明：当需要 Communicator 这个变量时，我们注入 DefaultCommunicatorImpl 的实例作为依赖
        bind(Fly.class).to(PlaneFly.class);
    }
}