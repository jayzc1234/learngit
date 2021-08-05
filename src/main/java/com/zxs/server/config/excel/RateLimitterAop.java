package com.zxs.server.config.excel;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect // 泛化advisor
@Component
public class RateLimitterAop {

    @Autowired
    private RateLimiter limiter;
    // spring源码中 通过拦截器链的配置与方法元信息匹配，链式执行匹配成功aop
    @Pointcut("@annotation(net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter)")
    public void excelPointCut(){

    }
    @Before(value = "excelPointCut()")
    public void beforeBiz(JoinPoint pj ) throws Throwable {
        // 限流则抛出异常
         limiter.canDoBiz();
     }
}
