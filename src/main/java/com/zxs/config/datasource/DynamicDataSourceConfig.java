package com.zxs.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"com.zxs.mapper"},sqlSessionFactoryRef="sqlSessionFactory")
public class DynamicDataSourceConfig {


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
		return factoryBean.getObject();
	}

	@Bean
	@Primary
	public SqlSession sqlSession() throws Exception {
		return new SqlSessionTemplate(sqlSessionFactory());
	}

    @Bean
    public PlatformTransactionManager fakeTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
