package zxs.mapper;

import zxs.pojo.Person;
import org.apache.ibatis.annotations.*;

public interface PersonMapper {

//    @Insert(
//            "<script>"+
//            "INSERT INTO " +
//            " person(name)" +
//            " VALUES " +
//            "<foreach collection='list' item='item'  separator=','> " +
//            "        (#{item.name}) " +
//            "    </foreach> "
//            +"</script>"
//    )
//    @Options(keyProperty="id",keyColumn="id",useGeneratedKeys=true)
//    public void batchInsert(@Param("list") List<Person> personList);
//
//    @Insert(
//            "INSERT INTO " +
//            " person(name)" +
//            " VALUES " +
//            "<foreach collection='list' item='item'  separator=','> " +
//            "        (#{item.name}) " +
//            "</foreach> "
//    )
//    @Options(keyProperty="id",keyColumn="id",useGeneratedKeys=true)
//    public List<Person> batchInsertNoScript(@Param("list") List<Person> personList);
//
//
//    @Select("<script>"
//              +"select * from person "
//              +"<where>"
//               +"<if test='id != null'>id=#{id}</if>"
//              +"</where>"
//            +"</script>"
//    )
//    public List<Person> listPerson(Integer id);
//
//
//    @Select("select * from person "
//            +"<where>"
//            +"<if test='id != null'>id=#{id}</if>"
//            +"</where>"
//    )
//    public List<Person> listPersonNoScript(Integer id);


    @Select("select * from person where id=${id}")
    public Person selectPersonNoDynamic(@Param("id") Integer id);

    @Select("update person set name =#{name} where id=${id}")
    public Person updateName(@Param("name") String name,@Param("id") Integer id);
}
