package com.zxs.cyclic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("cyclic2")
public class Cyclic1 {

    private Cyclic2 cyclic2;

    public Cyclic1(@Autowired Cyclic2 cyclic2){
        System.out.println("实例化 Cyclic1");
        this.cyclic2=cyclic2;
    }
}
