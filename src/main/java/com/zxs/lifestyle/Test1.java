package com.zxs.lifestyle;

import com.zxs.service.SonService1;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Data
public class Test1 implements InitializingBean, Lifecycle {

    private SonService1 sonService1;

    public String name;

    @PostConstruct
    public void init(){
        System.out.println("PostConstruct");
    }

    public Test1(){
        System.out.println("执行Test1的构造器");
    }
    @Autowired
    public void setService(SonService1 sonService1){
        System.out.println("setService");
        this.sonService1=sonService1;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InitializingBean afterPropertiesSet");
    }

    @Override
    public void start() {
        System.out.println("Test1 start");
    }

    @Override
    public void stop() {
        System.out.println("Test1 stop");
    }

    @Override
    public boolean isRunning() {
        return true;
    }
}
