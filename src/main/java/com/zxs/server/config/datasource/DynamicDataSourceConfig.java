package com.zxs.server.config.datasource;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import net.app315.hydra.intelligent.planting.common.gugeng.handler.MyMetaObjectHandler;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.user.data.auth.sdk.interceptor.MybatisAuthSqlInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@ConditionalOnClass(com.alibaba.druid.pool.DruidDataSource.class)
@ConditionalOnProperty(name = "spring.datasource.type", havingValue = "com.alibaba.druid.pool.DruidDataSource", matchIfMissing = true)
@MapperScan(basePackages = "net.app315.hydra.intelligent.planting.server.mapper",sqlSessionFactoryRef="sqlSessionFactory")
@EnableApolloConfig
public class DynamicDataSourceConfig {

    @Autowired
    private DynamicDataSourceProperties properties;

    @Autowired
	private MybatisAuthSqlInterceptor authSqlInterceptor;

	/**
	 * TODO 暂时从配置文件读取，后续考虑从数据库读，并且可通过接口动态添加、删除。实现参考
	 https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter/wikis/pages?sort_id=1030587&doc_id=147063
	 * 动态数据源设置，参考DynamicDataSourceAutoConfiguration
	 * 此处由于加入了公共的拦截器com.jgw.supercodeplatform.interceptor而是默认配置无法生效，故在此重新设置
	 * @param dynamicDataSourceProvider
	 * @return
	 */

	@Bean
    public DataSource dataSource(DynamicDataSourceProvider dynamicDataSourceProvider) {
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.setPrimary(this.properties.getPrimary());
        dataSource.setStrategy(this.properties.getStrategy());
        dataSource.setProvider(dynamicDataSourceProvider);
        dataSource.setP6spy(this.properties.getP6spy());
        return dataSource;
    }

	/**
	 * 数据源sqlSessionFactory配置
	 * 更多参数设置参考https://mp.baomidou.com/config/#%E8%BF%9B%E9%98%B6%E9%85%8D%E7%BD%AE
	 */
	@Bean
	@Primary
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource, PaginationInterceptor paginationInterceptor, GlobalConfig globalConfiguration, CommonUtil commonUtil) throws Exception {
		MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
		/* 数据源 */
		sqlSessionFactory.setDataSource(dataSource);
		/* 枚举扫描 */
//		sqlSessionFactory.setTypeEnumsPackage("net.app315.hydra.intelligent.planting.enums");
		/* entity扫描,mybatis的Alias功能 */
//		sqlSessionFactory.setTypeAliasesPackage("net.app315.hydra.intelligent.planting.pojo");
		/* entity扫描,mybatis的Alias功能 */
		MybatisConfiguration2 configuration = new MybatisConfiguration2();
		configuration.setJdbcTypeForNull(JdbcType.NULL);
		/* 驼峰转下划线 */
		configuration.setMapUnderscoreToCamelCase(true);
		/* 分页插件 */
		configuration.addInterceptor(paginationInterceptor);
		/* 允许JDBC支持自动生成主键 */
		configuration.setUseGeneratedKeys(true);
		/* 乐观锁插件 */
		configuration.addInterceptor(new OptimisticLockerInterceptor());
		configuration.addInterceptor(authSqlInterceptor);
		sqlSessionFactory.setConfiguration(configuration);
		sqlSessionFactory.setGlobalConfig(globalConfiguration);

		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		sqlSessionFactory.setMapperLocations(resolver.getResources("classpath:/mapper/*/*.xml"));
		return sqlSessionFactory.getObject();
	}

	public static void main(String[] args) throws IOException {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath:/mapper/*/*.xml");
		System.out.println(resources);
	}

	@Bean
	public GlobalConfig globalConfiguration(CommonUtil commonUtil) {
		GlobalConfig conf = new GlobalConfig();
		conf.setBanner(true)// 是否打印
		.setSqlInjector(new LogicSqlInjector()) // 逻辑注入sql
		.setMetaObjectHandler(new MyMetaObjectHandler(commonUtil))
		.setDbConfig(new GlobalConfig.DbConfig()
				.setLogicDeleteValue("1")
				.setLogicNotDeleteValue("0")
				.setIdType(IdType.AUTO));// 使用数据库生成方式
		return conf;
	}
	/**
	 * 分页插件，高级功能自行拓展
	 * @return
	 */
	@Bean
	public PaginationInterceptor paginationInterceptor() {
		PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
		return paginationInterceptor;
	}

    @Bean
    public PlatformTransactionManager fakeTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
