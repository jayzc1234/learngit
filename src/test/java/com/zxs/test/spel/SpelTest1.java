package com.zxs.test.spel;

import com.zxs.test.model.Inventor;
import com.zxs.test.model.Person;
import com.zxs.test.model.PlaceOfBirth;
import lombok.Data;
import org.junit.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;

public class SpelTest1 {
    public static void main(String[] args) {
        ExpressionParser parser=new SpelExpressionParser();
        Expression hello_world = parser.parseExpression("'hello world'");
        Object value = hello_world.getValue();
        System.out.println(value);
    }


    @Test
    public void test1(){
        ExpressionParser parser=new SpelExpressionParser();
        Expression hello_world = parser.parseExpression("'hello world'.concat('!')");
        String value = (String)hello_world.getValue();
        System.out.println(value);
    }

    @Test
    public void test2(){
        ExpressionParser parser=new SpelExpressionParser();
        Expression hello_world = parser.parseExpression("'hello world'.bytes.length");
        Object value =hello_world.getValue();
        System.out.println(value);
    }

    @Test
    public void test3(){
        ExpressionParser parser=new SpelExpressionParser();
        Expression hello_world = parser.parseExpression("new Person()");
        Person value = hello_world.getValue(Person.class);
        System.out.println(value);
    }

    @Test
    public void test4(){
      GregorianCalendar calendar=new GregorianCalendar();
      calendar.set(1898,7,9);
      Inventor tesla = new Inventor("Nikola Tesla", calendar.getTime(),"Serbian");
      PlaceOfBirth placeOfBirth=new PlaceOfBirth("hangzhou placeOfBirth");
      tesla.setPlaceOfBirth(placeOfBirth);
      ExpressionParser parser = new SpelExpressionParser();
      Expression exp = parser.parseExpression("placeOfBirth.city");
      Object value = exp.getValue(tesla);
      System.out.println(value);

        Expression expression = parser.parseExpression("name == 'jay'");
        Boolean value1 = expression.getValue(tesla, Boolean.class);
        System.out.println(value1);
    }
   @Test
   public void contextTypeConversion(){
     Simple simple=new Simple();
     simple.booleanList.add(true);
       //可以配置ExpressionParser使在属性为空时自动创建，数组或集合长度不够时自动扩容
       SpelParserConfiguration config = new SpelParserConfiguration(true,true);
       //关键的作用是为了类型转换
       EvaluationContext context = new StandardEvaluationContext();
       ExpressionParser parser = new SpelExpressionParser(config);
       parser.parseExpression("sList[0]").setValue(simple,new Date());
       String aBoolean = simple.sList.get(0);
       System.out.println(aBoolean);
   }

    @Test
    public void parserConfiguration(){
        Simple simple=new Simple();
        simple.booleanList.add(true);
        //可以配置ExpressionParser使在属性为空时自动创建，数组或集合长度不够时自动扩容
        SpelParserConfiguration config = new SpelParserConfiguration(true,true);
        //关键的作用是为了类型转换
        EvaluationContext context = new StandardEvaluationContext();
        ExpressionParser parser = new SpelExpressionParser(config);
        parser.parseExpression("sList[0]").setValue(simple,new Date());
        String aBoolean = simple.sList.get(0);
        System.out.println(aBoolean);
    }

    @Test
    public void compile(){
        long currentTimeMillis = System.currentTimeMillis();
        SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE,
                this.getClass().getClassLoader());
        SpelExpressionParser parser = new SpelExpressionParser(config);
        Expression expr = parser.parseExpression("name");
        Simple simple=new Simple();
        simple.setName("jay");
        Object payload = expr.getValue(simple);
        System.out.println(payload);
        long currentTimeMillis1 = System.currentTimeMillis();
        System.out.println(currentTimeMillis1-currentTimeMillis);
    }

   @Data
    class Simple{
        private String name;
        private List<Boolean> booleanList=new ArrayList<>();
        private List<String> sList=new ArrayList<>();
    }
}
