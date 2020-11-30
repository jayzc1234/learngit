package zxs.aop.advice;

import zxs.service.MyPrintService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInterceptor;

public class MyIntroductionInterceptor implements IntroductionInterceptor, MyPrintService {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (implementsInterface(invocation.getMethod().getDeclaringClass())){
            System.out.println("执行MyIntroductionInterceptor的invoke方法");
        }
      return invocation.getMethod().invoke(this,invocation.getArguments());
    }

    @Override
    public boolean implementsInterface(Class<?> intf) {
        return intf.isAssignableFrom(MyPrintService.class);
    }

    @Override
    public void print(String name) {
        System.out.println("执行MyIntroductionInterceptor的print方法");
    }
}
