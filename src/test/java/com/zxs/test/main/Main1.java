package com.zxs.test.main;

import com.zxs.test.model.NoJavaBean;
import org.springframework.beans.PropertyAccessor;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main1 {
    public static void main(String[] args) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        BeanInfo beanInfo= Introspector.getBeanInfo(NoJavaBean.class);
        BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
        NoJavaBean noJavaBean=new NoJavaBean();
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String name = propertyDescriptor.getName();
            Object name1 = propertyDescriptor.getValue(name);
            Class<?> propertyEditorClass = propertyDescriptor.getPropertyEditorClass();
            Method readMethod = propertyDescriptor.getReadMethod();
            Method writeMethod = propertyDescriptor.getWriteMethod();
        }
        MethodDescriptor[] methodDescriptors = beanInfo.getMethodDescriptors();
        for (MethodDescriptor methodDescriptor : methodDescriptors) {
            String name = methodDescriptor.getName();
            if (name.equals("test")){
                Method method = methodDescriptor.getMethod();
                Object invoke = method.invoke(noJavaBean);

                System.out.println(name);
            }
        }

    }
}
