package com.zxs.server.util.codegenerate2;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.io.File;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 自动生成pojo，dao, service, controller
 * <p>
 * 此方法尽量用于开发环境
 * 后续可采用idea插件 mybatiscodehelperpro 来完成单表基本业务代码的生成
 * 涉及第业务类中添加向相应的模版数据时可重写模版文件，在resources/templates下的 *.vm文件
 *
 * @author shixiongfei
 */
public class GeneratorServiceEntityUtil {

    private static final String DB_URL = "jdbc:mysql://192.168.2.215:3306/hydra_intelligent_planting?characterEncoding=utf-8&useSSL=false";

    private static final String USERNAME = "jgw";

    private static final String PASSWORD = "Jgw*31500-2018.6";

    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";

    private static final String PACKAGE_NAME = "net.app315.hydra.intelligent.planting";

    // 通用字段

    private static final String SYS_ID = "sys_id";

    private static final String ORGANIZATION_ID = "organization_id";

    private static final String CREATE_TIME = "create_time";

    private static final String UPDATE_TIME = "update_time";

    private static final String OPERATOR = "operator";

    private static final String OPERATOR_NAME = "operator_name";

//    private static final String IS_DELETED = "is_deleted";

    /**
     * 在这里生成的文件仅仅在controller, dao, pojo, service下
     * 如需配置，可在{@code getTemplateConfig()}方法中进行操作
     *
     * @param args
     */
    public static void main(String[] args) throws SQLException {
        System.out.println("--------------------开始自动生成相关的类----------------------");
        System.out.println("当前模块根路径为:" + new File("hydra-intelligent-planting").getAbsolutePath());
        // 输入需要执行代码生成的数据库表名，支持多表，以逗号隔开
//        ArrayList<String> hydra_intelligent_planting = MySqlMetaData.getTableNameList("hydra_intelligent_planting");
        generateByTables("hydra-intelligent-planting-server","t_product_level_maintain");
        System.out.println("--------------------------生成成功------------------------");
    }


    /**
     * 代码生成调用器
     *
     * @param module     模块名称
     * @param tableNames 表名集合
     */
    private static void generateByTables(String module, String... tableNames) {
        generatorClasses(module, tableNames);
    }


    /**
     * 代码生成执行器
     *
     * @param tableNames
     */
    private static void generatorClasses(String module, String[] tableNames) {

        // 全局配置
        GlobalConfig globalConfig = getGlobalConfig(module);

        // 数据源配置
        DataSourceConfig dataSourceConfig = getDataSourceConfig();

        // 包配置
        PackageConfig packageConfig = getPackageConfig();

        // 策略配置
        StrategyConfig strategyConfig = getStrategyConfig(tableNames);
        strategyConfig.setEntityTableFieldAnnotationEnable(true);
        // 配置模板
        TemplateConfig templateConfig = getTemplateConfig();

        // 采用自定义代码生成器来完成
        new MyAutoGenerator()
                .setGlobalConfig(globalConfig)
                .setDataSource(dataSourceConfig)
                .setPackageInfo(packageConfig)
                .setStrategy(strategyConfig)
                .setTemplate(templateConfig)
                .execute();

    }

    /**
     * 自定义代码生成模板
     *
     * @return
     */
    private static TemplateConfig getTemplateConfig() {
        TemplateConfig templateConfig = new TemplateConfig();
        // entity模板采用自定义模板
        templateConfig.setEntity("templates/entity.java.vm")
                // mapper模板采用自定义模板
                .setMapper("templates/mapper.java.vm")
                .setService("templates/service.java.vm")
                // serviceImpl模板采用自定义模板
                .setServiceImpl("templates/serviceImpl.java.vm")
                // controller模板采用自定义模板
                .setController("templates/controller.java.vm")
                // 不生成xml文件
                .setXml(null);
        return templateConfig;
    }

    /**
     * 定义策略
     *
     * @param tableNames
     * @return
     */
    private static StrategyConfig getStrategyConfig(String... tableNames) {
        StrategyConfig strategyConfig = new StrategyConfig();
        //驼峰命名
        strategyConfig.setCapitalMode(true)
                .setEntityLombokModel(true)
                .setRestControllerStyle(true)
                .setNaming(NamingStrategy.underline_to_camel)
                .setColumnNaming(NamingStrategy.underline_to_camel)
                // 强制添加@TableField注解
                .setEntityTableFieldAnnotationEnable(true)
                .setInclude(tableNames)
                // boolean类型的字段移除is_前缀
                .setEntityBooleanColumnRemoveIsPrefix(true)
                //3.1.0版本的代码生成器需要设置表名前缀，否则生成的pojo不会生成表名，而新版本的只需设置setEntityBooleanColumnRemoveIsPrefix(true)
               .setTablePrefix("t_")
                .setTableFillList( // 配置自动注入的字段
                        Stream.of(new TableFill(SYS_ID, FieldFill.INSERT),
                                new TableFill(ORGANIZATION_ID, FieldFill.INSERT),
                                new TableFill(CREATE_TIME, FieldFill.INSERT),
                                new TableFill(UPDATE_TIME, FieldFill.INSERT_UPDATE),
                                new TableFill(OPERATOR, FieldFill.INSERT),
                                new TableFill(OPERATOR_NAME, FieldFill.INSERT)
//                                new TableFill(IS_DELETED, FieldFill.INSERT)
                        ).collect(Collectors.toList()));
        return strategyConfig;
    }

    /**
     * 配置生成包名
     *
     * @return
     */
    private static PackageConfig getPackageConfig() {
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent(PACKAGE_NAME)
                .setEntity("model")
                .setMapper("mapper")
                .setService("service")
                .setServiceImpl("service.impl")
                .setController("controller");
        return packageConfig;
    }

    /**
     * 配置数据源
     *
     * @return
     */
    private static DataSourceConfig getDataSourceConfig() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL)
                .setDriverName(DRIVER_NAME)
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setUrl(DB_URL);
        return dataSourceConfig;
    }

    /**
     * 全局配置，配置生成文件的目录
     *
     * @return
     */
    private static GlobalConfig getGlobalConfig(String module) {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOpen(false)
                // new File("hydra-trace").getAbsolutePath()得到当前模块根目录路径
                //生成文件的输出目录
                .setOutputDir(new File(module).getAbsolutePath() + "/src/main/java")
                //是否覆盖已有文件
                .setFileOverride(true)
                .setBaseResultMap(true)
                .setBaseColumnList(true)
                .setActiveRecord(false)
                .setAuthor("shixiongfei")
                .setServiceName("%sService");
        return globalConfig;
    }
}