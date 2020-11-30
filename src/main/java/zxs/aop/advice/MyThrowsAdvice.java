package zxs.aop.advice;

import org.springframework.aop.ThrowsAdvice;

public class MyThrowsAdvice implements ThrowsAdvice{

    public void afterThrowing(Exception ex){
        System.out.println("执行MyThrowsAdvice的afterThrowing方法");
    }
}
