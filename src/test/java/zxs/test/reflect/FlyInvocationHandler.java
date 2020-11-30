package zxs.test.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class FlyInvocationHandler implements InvocationHandler {

    private Object object;


    public FlyInvocationHandler(Object object){
        this.object = object ;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("执行fly代理逻辑");
        return method.invoke(object,args);
    }
}
