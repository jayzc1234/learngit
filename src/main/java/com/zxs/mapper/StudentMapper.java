package com.zxs.mapper;

import com.zxs.pojo.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 学生mapper
 * @author zc
 */
@Mapper
public interface StudentMapper {

    @Select("select * from person limit 1")
    Student selectById();
}
