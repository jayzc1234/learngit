package com.zxs.service;

import com.zxs.mapper.PersonMapper;
import com.zxs.mapper.StudentMapper;
import com.zxs.pojo.Student;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zc
 */
@Component
public class PersonService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private PersonMapper personMapper;

    public void test(){
        RowBounds rowBounds=new RowBounds(1,2);
        List<Student> student2=studentMapper.page(rowBounds);
        System.out.println(student2);
    }
}
