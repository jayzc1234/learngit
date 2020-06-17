package com.zxs.mapper;

import com.zxs.pojo.Person;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface PersonMapper {

    @Insert("<script>"+
            "INSERT INTO " +
            " person(name)" +
            " VALUES " +
            "<foreach collection='list' item='item'  separator=','> " +
            "        (#{item.name}) " +
            "    </foreach> "
            +"</script>"
    )
    @Options(keyProperty="id",keyColumn="id",useGeneratedKeys=true)

    public void batchInsert(List<Person> personList);
}
