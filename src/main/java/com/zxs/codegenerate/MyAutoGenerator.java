package com.zxs.codegenerate;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;

import java.util.List;
import java.util.Set;

/**
 * 自定义代码生成器, 后续需要拓展，可在getAllTableInfoList方法中进行拓展
 *
 * @author shixiongfei
 */
public class MyAutoGenerator extends AutoGenerator {

    private static final String LOCAL_DATE_TIME = "LocalDateTime";

    private static final String PACKAGE_LOCAL_DATE_TIME = "java.time.LocalDateTime";

    private static final String PACKAGE_TIME = "java.util.Date";

    private static final String T = "T";

    private static final String REMOVE_IS = "is";


    private String removeT(String name) {
        if (name.startsWith(T)) {
            return upperFirst(name.substring(1));
        }
        return name;
    }

    /**
     * 大写第一个字母
     *
     * @param src 源字符串
     * @return 返回第一个大写后的字符串
     */
    public static String upperFirst(String src) {
        if (Character.isLowerCase(src.charAt(0))) {
            return 1 == src.length() ? src.toUpperCase() : Character.toUpperCase(src.charAt(0)) + src.substring(1);
        }
        return src;
    }

    @Override
    protected List<TableInfo> getAllTableInfoList(ConfigBuilder config) {
        StrategyConfig strategyConfig = config.getStrategyConfig();
        // 自动生成数据库字段常量，便于日后的sql查询
        strategyConfig.setEntityColumnConstant(true);
        List<TableInfo> allTableInfoList = super.getAllTableInfoList(config);

        allTableInfoList.forEach(tableInfo -> {
            // 移除开头的t
            tableInfo.setEntityName(removeT(tableInfo.getEntityName()));
            tableInfo.setServiceName(removeT(tableInfo.getServiceName()));
            tableInfo.setServiceImplName(removeT(tableInfo.getServiceImplName()));
            tableInfo.setMapperName(removeT(tableInfo.getMapperName()));
            tableInfo.setControllerName(removeT(tableInfo.getControllerName()));

            List<TableField> fields = tableInfo.getFields();
            Set<String> importPackages = tableInfo.getImportPackages();

            fields.forEach(field -> {
                // 如果字段开头为is则移除
                if (field.getPropertyName().startsWith(REMOVE_IS)) {
                    String propertyName = StringUtils.firstToLowerCase(field.getPropertyName().substring(2));
                    field.setPropertyName(propertyName);
                }
                // 如果存在LocalDateTime类型时，将其修改为Date类型
                if (LOCAL_DATE_TIME.equals(field.getPropertyType())) {
                    field.setColumnType(DbColumnType.DATE);
                    importPackages.remove(PACKAGE_LOCAL_DATE_TIME);
                    importPackages.add(PACKAGE_TIME);
                }
            });
        });

        // 可自行添加自定义代码生成模版方法，如下所示：
        // config.setInjectionConfig(getInjectionConfig(config));
        return allTableInfoList;
    }
}