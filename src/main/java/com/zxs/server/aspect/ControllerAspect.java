package com.zxs.server.aspect;


import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.utils.fuding.AspectUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @Description:    Controller 切面
 * @Author:         xiaoliang.chen
 * @Date:     2019/2/21 上午11:45
 */
@Aspect
@Slf4j
@Component
public class ControllerAspect {

    @Around("execution(public * net.app315.hydra.human.construction.server.controller.*Controller.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        return AspectUtil.logAround(joinPoint , 3000L);
    }
}
