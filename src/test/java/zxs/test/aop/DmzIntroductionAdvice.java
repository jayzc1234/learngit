package zxs.test.aop;

import org.springframework.aop.support.DelegatingIntroductionInterceptor;

public class DmzIntroductionAdvice extends DelegatingIntroductionInterceptor implements Runnable {
    @Override
    public void run() {
        System.out.println("running----");
    }
}
