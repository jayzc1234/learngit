//package com.zxs.config.datasource;
//
//import org.apache.commons.dbcp2.BasicDataSource;
//import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
//import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
//import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
//import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
//
//import javax.sql.DataSource;
//import java.sql.SQLException;
//import java.util.*;
//
///**
// *  sharding-jdbc
// * @author zc
// */
//public class ShardingJdbcDataSource {
//
//    public DataSource getDatasource() throws SQLException {
//        // Configure actual data sources
//        Map<String, DataSource> dataSourceMap = new HashMap<>();
//
//        // Configure the first data source
//        BasicDataSource dataSource1 = new BasicDataSource();
//        dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
//        dataSource1.setUrl("jdbc:mysql://localhost:3309/ds0");
//        dataSource1.setUsername("root");
//        dataSource1.setPassword("123456");
//        dataSourceMap.put("ds0", dataSource1);
//
//        // Configure the second data source
//        BasicDataSource dataSource2 = new BasicDataSource();
//        dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
//        dataSource2.setUrl("jdbc:mysql://localhost:3309/ds1");
//        dataSource2.setUsername("root");
//        dataSource2.setPassword("");
//        dataSourceMap.put("ds1", dataSource2);
//
//        // Configure Order table rules
//        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration("t_order","ds${0..1}.t_order${0..1}");
//        // Configure strategies for database + table sharding
//        orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
//        orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order${order_id % 2}"));
//
//        // Configure sharding rules
//        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
//        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
//
//        // Omit order_item table rule configuration
//        // ...
//
//        // Get data source
//        DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new Properties());
//        return dataSource;
//    }
//}
