package com.zxs.server.util.codegenerate;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySqlMetaData {

    public static void main(String[] args) {
        getOldColumnMap("jgw_product_management");
    }

    public static Map<String, List<ColumnDesc>> getOldColumnMap(String dataBase) {
        // TODO Auto-generated method stub
        //variables used to operate the mysql database
        Connection connection = getJgwConnection(dataBase);
        ResultSet resultSet;
        Map<String, List<ColumnDesc>> tableMap = new HashMap<>();
        try {
            DatabaseMetaData dbmd = connection.getMetaData();
            ArrayList<String> tList = getTableNameList(dataBase);
            for (String tn : tList) {
                List<ColumnDesc> columnDescList = new ArrayList<>();
                try {
                    resultSet = dbmd.getPrimaryKeys(null, null, tn);
                    String primaryKeyName =null;
                    while(resultSet.next()) {
                        primaryKeyName = resultSet.getString("COLUMN_NAME");
                        System.out.println("Primykey Column name:" + primaryKeyName );
                    }
                    resultSet = dbmd.getIndexInfo(null, null, tn, false, false);
                    while (resultSet.next()) {
                        String name = resultSet.getString("COLUMN_NAME");
                        System.out.println("Index Column name:" + name );
                    }
                    resultSet = dbmd.getColumns(null, null, tn, null);
                    while (resultSet.next()) {
                        String name = resultSet.getString("COLUMN_NAME");
                        String type = resultSet.getString("TYPE_NAME");
                        int size = resultSet.getInt("COLUMN_SIZE");
                        if (type.equalsIgnoreCase("bit")){
                            type = "tinyint";
                            size = 2;
                        }
                        String def = resultSet.getString("COLUMN_DEF");
                        String remarks = resultSet.getString("REMARKS");
                        String is_nullable = resultSet.getString("IS_NULLABLE");
                        String table_cat = resultSet.getString("TABLE_CAT");
                        String scope_table = resultSet.getString("SCOPE_TABLE");
                        String table_name = resultSet.getString("TABLE_NAME");
                        String table_schem = resultSet.getString("TABLE_SCHEM");
                        String type_name = resultSet.getString("TYPE_NAME");
                        String DECIMAL_DIGITS = resultSet.getString("DECIMAL_DIGITS");
                        if (primaryKeyName.equals(name)){
                            ColumnDesc columnDesc = new ColumnDesc(tn,name,type,size,remarks,is_nullable,def,true,DECIMAL_DIGITS);
                            columnDescList.add(columnDesc);
                        }else {
                            ColumnDesc columnDesc = new ColumnDesc(tn,name,type,size,remarks,is_nullable,def,false,DECIMAL_DIGITS);
                            columnDescList.add(columnDesc);
                        }
                        System.out.println("Column name: [" + name + "]; type: [" + type + "]; size: [" + size + "]");
                    }
                    tableMap.put(tn,columnDescList);
                } catch (Exception e) {
                    // TODO: handle exception
                    System.out.println(e);
                }
                //break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("java connection to mysql is ending...");
        return tableMap;
    }

    public static ArrayList<String> getTableNameList(String dataBase) throws SQLException {
        // 表名列表
        Connection connection = getJgwConnection(dataBase);
        DatabaseMetaData metaData = connection.getMetaData();
        String[] types = {"TABLE"};
        ArrayList<String> tList = new ArrayList<String>();
        ResultSet rest = metaData.getTables(null, null, "%", types);
        while (rest.next()) {
            String tablename = rest.getString("TABLE_NAME");
//                String remarks = rest.getString("REMARKS");
//                String table_cat = rest.getString("TABLE_CAT");
//                String type_cat = rest.getString("TYPE_CAT");
//                String table_name = rest.getString("TABLE_NAME");
//                String table_schem = rest.getString("TABLE_SCHEM");
//                String type_name = rest.getString("TYPE_NAME");
            //System.out.println(tablename);
            tList.add(tablename);
        }
        return tList;
    }

    public static Connection getJgwConnection(String dataBase) {
        Connection connection = null;
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;
        //the information of mysql database
        String user = "jgw";
        String password = "Jgw*31500-2018.6";
        String url = "jdbc:mysql://192.168.2.214:3306/"+dataBase+"?characterEncoding=utf-8&useSSL=false&tinyInt1isBit=false";
        //look up the mysql diriver , is it useful ?
        try{
            Class.forName("com.mysql.jdbc.Driver");
        }catch(ClassNotFoundException e){
            System.out.println("mysql driver is not ready ...");
            e.printStackTrace();
        }
        try{
            connection = DriverManager.getConnection(url,user,password);//url,user,password
        }catch(SQLException e){
            System.out.println("there is sql exception when connecting ...");
            e.printStackTrace();
        }
        return connection;
    }

    public static Connection getConnection() {
        Connection connection = null;
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;
        //the information of mysql database
        String user = "root";
        String password = "123456";
        String url = "jdbc:mysql://localhost:3306/mycatdb1?tinyInt1isBit=false";
        //look up the mysql diriver , is it useful ?
        try{
            Class.forName("com.mysql.jdbc.Driver");
        }catch(ClassNotFoundException e){
            System.out.println("mysql driver is not ready ...");
            e.printStackTrace();
        }
        try{
            connection = DriverManager.getConnection(url,user,password);//url,user,password
        }catch(SQLException e){
            System.out.println("there is sql exception when connecting ...");
            e.printStackTrace();
        }
        return connection;
    }
}
