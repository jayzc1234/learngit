package zxs.test.aop;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

public class DmzBeforeAdvice implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("before invoke method [" + method.getName() + "],aop before logic invoked");
    }
}
