package com.zxs.service;

import com.zxs.mapper.StudentMapper;
import com.zxs.pojo.Student;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author zc
 */
@Component
public class PersonService {

    @Autowired
    private StudentMapper studentMapper;

    public void test(){
        Student student=studentMapper.selectById();
    }
}
