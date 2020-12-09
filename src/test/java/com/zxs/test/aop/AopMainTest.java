package com.zxs.test.aop;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

public class AopMainTest {

    public static void main(String[] args) {
        ProxyFactory proxyFactory = new ProxyFactory();
        //一个advisor代表的是一个已经跟指定切点绑定了的通知
        Advisor advisor = new DefaultPointcutAdvisor(new DmzPointcut(),new DmzAroundAdvice());
        proxyFactory.addAdvisor(advisor);

        //添加一个返回后的通知
        proxyFactory.addAdvice(new DmzAfterReturnAdvice());

        proxyFactory.addAdvice(new DmzBeforeAdvice());

        //为代理类额外添加一个方法
        proxyFactory.addAdvice(new DmzIntroductionAdvice());

        //设值目标类
        proxyFactory.setTarget(new DmzService());

        proxyFactory.setProxyTargetClass(true);

        Object proxy = proxyFactory.getProxy();

        String s = proxy.toString();

        if (proxy instanceof DmzService){
            ((DmzService) proxy).testAop();
        }

        if (proxy instanceof  Runnable){
            ((Runnable) proxy).run();
        }
    }
}
