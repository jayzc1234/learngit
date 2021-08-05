package com.zxs.server.mapper.gugeng;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author zc
 * 批量更新表部门信息
 */
@Mapper
public interface TempAuthMapper {

    @Update("${sql}")
    void update(@Param("sql") String sql);

    @Select("${sql}")
    List<String> select(@Param("sql") String sql);

}
