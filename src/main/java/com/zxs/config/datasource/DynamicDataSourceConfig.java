package com.zxs.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.zxs.pojo.BeanAnnotation;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass(com.alibaba.druid.pool.DruidDataSource.class)
@ConditionalOnProperty(name = "spring.datasource.type", havingValue = "com.alibaba.druid.pool.DruidDataSource", matchIfMissing = true)
@MapperScan(basePackages = {"com.zxs.mapper"},sqlSessionFactoryRef="sqlSessionFactory")
public class DynamicDataSourceConfig {

	@Bean
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS,value = WebApplicationContext.SCOPE_REQUEST)
	public BeanAnnotation beanAnnotation(){
		return new BeanAnnotation();
	}

//    @Autowired
//	private CommonUtil commonUtil;

	/**
	 * TODO 暂时从配置文件读取，后续考虑从数据库读，并且可通过接口动态添加、删除。实现参考
	 https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter/wikis/pages?sort_id=1030587&doc_id=147063
	 * 动态数据源设置，参考DynamicDataSourceAutoConfiguration
	 * 此处由于加入了公共的拦截器com.jgw.supercodeplatform.interceptor而是默认配置无法生效，故在此重新设置
	 * @return
	 */

	@Bean
    public DataSource dataSource() {
		DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return dataSource;
    }

	/**
	 * 数据源sqlSessionFactory配置
	 * 更多参数设置参考https://mp.baomidou.com/config/#%E8%BF%9B%E9%98%B6%E9%85%8D%E7%BD%AE
	 */
	@Bean
	@Primary
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource());
//		org.apache.ibatis.session.Configuration configuration=new org.apache.ibatis.session.Configuration();
//		configuration.setDefaultExecutorType(ExecutorType.BATCH);
//		factoryBean.setConfiguration(configuration);
		return factoryBean.getObject();
	}

	@Bean
	@Primary
	public SqlSessionTemplate sqlSessionTemplate() throws Exception {
		return new SqlSessionTemplate(sqlSessionFactory(), ExecutorType.BATCH);
	}

    @Bean
    public PlatformTransactionManager fakeTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }



}
