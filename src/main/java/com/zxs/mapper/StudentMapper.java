package com.zxs.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.zxs.pojo.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 学生mapper
 * @author zc
 */
public interface StudentMapper {

    @Select("select * from person limit #{current}, #{pageSize}")
    List<Student> selectById(@Param("current") int current, @Param("pageSize")int pageSize);

    @Select("select * from person ")
    List<Student> page(RowBounds rowBounds);
}
