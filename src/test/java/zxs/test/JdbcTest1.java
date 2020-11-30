package zxs.test;


import java.sql.DriverManager;
import java.awt.List;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

public class JdbcTest1 {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        //variables used to operate the mysql database
        Connection connection = null;
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;
        //the information of mysql database
        String user = "root";
        String password = "123456";
        String url = "jdbc:mysql://localhost:3306/mycatdb1";
        //look up the mysql diriver , is it useful ?
        try{
            Class.forName("com.mysql.jdbc.Driver");
        }catch(ClassNotFoundException e){
            System.out.println("mysql driver is not ready ...");
            e.printStackTrace();
        }
        // open a mysql connection to java application
        try{
            connection = DriverManager.getConnection(url,user,password);//url,user,password
        }catch(SQLException e){
            System.out.println("there is sql exception when connecting ...");
            e.printStackTrace();
        }
        ArrayList<String> tList = new ArrayList<String>();
        try {
            DatabaseMetaData dbmd = connection.getMetaData();
            // 表名列表
            String[] types = {"TABLE"};
            ResultSet rest = dbmd.getTables(null, null, "%", types);
            while (rest.next()) {
                String tablename = rest.getString("TABLE_NAME");
                //System.out.println(tablename);
                tList.add(tablename);
            }
            for (String tn : tList) {
                System.out.println(tn);
                try {
                    resultSet = dbmd.getPrimaryKeys(null, null, tn);
                    while(resultSet.next()) {
                        String name = resultSet.getString("COLUMN_NAME");
                        System.out.println("Primykey Column name:" + name );
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
                		      System.out.println("Column name: [" + name + "]; type: [" + type + "]; size: [" + size + "]");
                		}
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
    }

}

