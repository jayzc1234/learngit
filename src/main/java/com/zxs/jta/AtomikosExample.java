package com.zxs.jta;

import com.atomikos.jdbc.AtomikosDataSourceBean;

import java.util.Properties;

public class AtomikosExample {

    private static AtomikosDataSourceBean createAtomikosDataSourceBean(String dbName){
        Properties p = new Properties();
        p.setProperty("url", "jdbc:mysql://localhost:3306/" + dbName);
        p.setProperty("user", "root");
        p.setProperty("password", "123456");

        //使用AtomikosDataSourceBean封装com.jdbc.jdbc2.optional.MysqlXADataSource
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        ds.setUniqueResourceName(dbName);
        ds.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        ds.setXaProperties(p);
        return ds;
    }

    public static void main(String[] args) {
        AtomikosDataSourceBean ds1 =createAtomikosDataSourceBean("db_user");
        AtomikosDataSourceBean ds2 =createAtomikosDataSourceBean("db_account");
    }


}
