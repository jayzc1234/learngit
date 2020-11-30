package zxs.aop.advice;

import zxs.pojo.Person;
import zxs.service.MyPrintService;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.cglib.core.DebuggingClassWriter;

public class MainTestAop1 {
    public static void main(String[] args) {
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "F:\\code");
        Person person = new Person();
        person.setId(1);
        ProxyFactory proxyFactory =new ProxyFactory(person);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new MyMethodBeforeAdvice());
        proxyFactory.addAdvice(new MyAfterReturningAdvice());
        proxyFactory.addAdvice(new MyThrowsAdvice());
        proxyFactory.addAdvice(new AroundAdvice());
        proxyFactory.addAdvisor(new DefaultIntroductionAdvisor(new MyIntroductionInterceptor(),MyPrintService.class));
        MyPrintService proxy = (MyPrintService) proxyFactory.getProxy();
        proxy.print("jay");
    }
}
