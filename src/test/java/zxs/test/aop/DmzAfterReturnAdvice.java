package zxs.test.aop;

import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public class DmzAfterReturnAdvice implements AfterReturningAdvice {
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("after invoke method [" + method.getName() + "],aop afterReturning logic invoked");
    }
}
