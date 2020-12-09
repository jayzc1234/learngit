package com.zxs.cyclic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Cyclic2 {

    @Autowired
    private Cyclic1 cyclic1;

}
