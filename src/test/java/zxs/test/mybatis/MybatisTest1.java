package zxs.test.mybatis;

import zxs.ApplicationBootApp;
import zxs.mapper.PersonMapper;
import zxs.pojo.Person;
import zxs.service.PersonService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class) // SpringJUnit支持，由此引入Spring-Test框架支持！
@SpringBootTest(classes = ApplicationBootApp.class) // 指定我们SpringBoot工程的Application启动类
public class MybatisTest1 {
    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private PersonService personService;

    @Test
    public void updatePerson1() throws InterruptedException {
        personService.update1("name1",7);
    }

    @Test
    public void updatePerson2(){
        personService.update2("name2",7);
    }

    @Test
    public void batchInsert(){
        Person person1=new Person();
        person1.setName("jay1");
        Person person2=new Person();
        person2.setName("jay2");

        List<Person> personList=new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

//        MetaObject metaObject = configuration.newMetaObject(personList);
//        Object value = metaObject.getValue("item.name");

//        List<Person> people = personMapper.listPerson(1);
//        System.out.println(people);
//        personMapper.batchInsert(personList);
//        System.out.println(personList);
    }

    @Test
    public void batchInsertNoScript(){
        Person person1=new Person();
        person1.setName("jay1");
        Person person2=new Person();
        person2.setName("jay2");

        List<Person> personList=new ArrayList<>();
        personList.add(person1);
        personList.add(person2);
//        List<Person> people = personMapper.batchInsertNoScript(personList);
//        System.out.println(people);
    }

    @Test
    public void selectPersonNoDynamic(){
        Person people = personMapper.selectPersonNoDynamic(7);
        System.out.println(people);
    }

    @Test
    public void listPersonNoScript(){
//        List<Person> people = personMapper.listPersonNoScript(1);
//        System.out.println(people);
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        char a = '\0';
        char b = ' ';
        System.out.println(a == b);

        XMLLanguageDriver languageDriver = new XMLLanguageDriver();
        Configuration configuration = new Configuration();
        String script = noScriptSql();
        Person person = new Person();
        person.setId(1);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, script, Person.class);
        BoundSql boundSql = sqlSource.getBoundSql(person);
        String sql = boundSql.getSql();
        System.out.println(sql);
    }

    private static String getScriptSql() {
        return "<script>"
                    +"select * from person "
                    +"<where>"
                    +"<if test='id != null'>id=#{id}</if>"
                    +"</where>"
                    +"</script>";
    }

    private static String noScriptSql() {
        return "select * from person "
                +"<where>"
                +"<if test='id != null'>id=#{id}</if>"
                +"</where>";
    }

    private static String noScriptNoDynamicSql() {
        return "select * from person "
                +"where id = #{id}";
    }

    private static String noScriptNoDynamicWith$Sql() {
        return "select * from person "
                +"where id = ${1}";
    }

    private static void mainTest1() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setValidating(false);

        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        // builder.setEntityResolver(new XMLMapperEntityResolver());
        InputSource inputSource = new InputSource(Resources.getResourceAsStream("mybatis-config.xml"));

        Document document = builder.parse(inputSource);

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        String value = (String) xpath.evaluate("/configuration/settings/setting[@name='defaultStatementTimeout']/@value", document, XPathConstants.STRING);

        System.out.println("defaultStatementTimeout=\"" + value + "\"");
        Node node = (Node) xpath.evaluate("/configuration/mappers/mapper[1]", document, XPathConstants.NODE);
        NamedNodeMap attributeNodes = node.getAttributes();
        for (int i = 0; i < attributeNodes.getLength(); i++) {
            Node n = attributeNodes.item(i);
            System.out.println(n.getNodeName() + "=\"" + n.getNodeValue() + "\"");
        }
    }
}
