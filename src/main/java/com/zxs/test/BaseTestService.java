package com.zxs.test;

import org.springframework.beans.factory.InitializingBean;

public abstract class BaseTestService implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet");
    }
}
