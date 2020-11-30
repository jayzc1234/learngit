package zxs.test.jvm;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class StackOOM {

    public static void main(String[] args) {
        test1();
    }

    private static void test1() {
        StackOOM stackOOM=new StackOOM();
        long i=0;
        while (true){
            i++;
        }
    }

    public void test(){
        int i=0;
        while (true){
            i++;
            test();
        }
    }

    public static  void test2(){
        while (true){
            org.springframework.cglib.proxy.Enhancer enhancer=new Enhancer();
            enhancer.setSuperclass(HeapOOM.class);
            enhancer.setUseCache(false);
            enhancer.setCallback(new MethodInterceptor() {
                @Override
                public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                    return methodProxy.invokeSuper(o,objects);
                }
            });
            enhancer.create();
        }
    }
}
