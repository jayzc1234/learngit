package com.zxs.test.mybatis;

import com.zxs.ApplicationBootApp;
import com.zxs.mapper.PersonMapper;
import com.zxs.pojo.Person;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
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
        personMapper.batchInsert(personList);
        System.out.println(personList);
    }
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
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
