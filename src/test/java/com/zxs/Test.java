package com.zxs;

import com.alibaba.fastjson.JSON;
import com.zxs.pojo.Order;
import com.zxs.pojo.Student;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.concurrent.FastThreadLocal;
import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransaction;

import javax.sql.DataSource;
import java.beans.BeanInfo;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) throws Exception {
        Method getId = Order.class.getDeclaredMethod("getData");
        Method getId2 = Order.class.getDeclaredMethod("getData");
        Order order = new Order();
        order.setId(1);
        getId.setAccessible(true);
        Object invoke = getId.invoke(order);
        System.out.println(getId==getId2);
    }

    @org.junit.Test
    public  void memory() {
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        ByteBuf buf = allocator.directBuffer(1);
        buf.release();
        allocator.directBuffer(1);
    }


    @org.junit.Test
    public  void fastThreadLocal() {
        FastThreadLocal<Integer> fastThreadLocal = new FastThreadLocal<>();
        fastThreadLocal.set(1);
        fastThreadLocal.set(2);
        FastThreadLocal<Integer> fastThreadLocal1 = new FastThreadLocal<>();
        fastThreadLocal1.set(1);
        fastThreadLocal1.remove();
        Integer integer = fastThreadLocal.get();
        System.out.println(integer);
    }

    @org.junit.Test
    public  void poolThreadLocalCache() {
    }


    @org.junit.Test
    public  void objectPool() {

    }

}