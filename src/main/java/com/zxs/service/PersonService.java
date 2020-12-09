package com.zxs.service;

import com.zxs.mapper.PersonMapper;
import com.zxs.mapper.StudentMapper;
import com.zxs.pojo.Student;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zc
 */
@Component
public class PersonService <T extends List<String>>{

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private PersonMapper personMapper;

    public void test(){
        RowBounds rowBounds=new RowBounds(1,2);
        List<Student> student2=studentMapper.page(rowBounds);
        System.out.println(student2);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update1(String name, Integer id) throws InterruptedException {
        personMapper.updateName(name,id);
        System.out.println("update1方法进入睡眠");
        Thread.sleep(20000);
        System.out.println("update1方法运行结束");
    }

    @Transactional(rollbackFor = Exception.class)
    public  void  update2(String name, Integer id){
        System.out.println("开始执行update2方法");
        personMapper.updateName(name,id);
        System.out.println("update2方法运行结束");
    }
}
