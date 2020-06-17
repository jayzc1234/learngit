package com.zxs.test.main;

import com.zxs.test.model.NoJavaBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.validation.DataBinder;

@EnableAspectJAutoProxy
public class DataBinderMain {
    public static void main(String[] args) {
        NoJavaBean noJavaBean=new NoJavaBean();
        DataBinder dataBinder=new DataBinder(noJavaBean);
        MutablePropertyValues mutablePropertyValues=new MutablePropertyValues();
        mutablePropertyValues.addPropertyValue("id",1);
        mutablePropertyValues.addPropertyValue("name","jay");
        dataBinder.bind(mutablePropertyValues);
        System.out.println(noJavaBean);

    }
}
