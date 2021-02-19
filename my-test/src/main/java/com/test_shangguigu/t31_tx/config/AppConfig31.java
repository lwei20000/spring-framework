package com.test_shangguigu.t31_tx.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 声明式食物：
 *
 * 环境搭建：
 * 1、导入相关依赖
 *     数据源、数据库驱动、SpringJdbc模块
 * 2、配置数据源、JdbcTemplate（Spring提供的简化数据库操作工具）操作数据
 * 3、给方法上标注 @Transactional 表示当前方法是一个事务方法；
 * 4、@EnableTransactionManagement 开启基于注解的事务管理功能
 *        @EnableXXX
 * 5、配置事务管理器来管理事务
 *        @Bean
 *        public PlatformTransactionManager transactionManager()
 *
 * 原理
 * 1、@EnableTransactionManagement
 *     利用TransactionManagementConfigurationSelector给容器中会导入
 *     导入两个组件：
 *     （1）AutoProxyRegistrar
 *     （2）ProxyTransactionManagementConfiguration
 * 2、AutoProxyRegistrar；
 *     给容器中注册一个 InfrastructureAdvisorAutoProxyCreator 组件
 *
 *     【类比，事务跟aop方式是类似的】
 *     AOP ：AnnotationAwareAspectJAutoProxyCreator 《 SmartInstantiationAwareBeanPostProcessor
 *     TX  ：InfrastructureAdvisorAutoProxyCreator  《 SmartInstantiationAwareBeanPostProcessor
 *     AOP和TX的机制都是一样的：利用后置处理器机制在对象创建以后，包装对象，返回一个代理对象（增强器），代理对象执行方法利用拦截器链进行调用。
 *
 * 3、ProxyTransactionManagementConfiguration
 *     1）给容器中注册事务增强器： BeanFactoryTransactionAttributeSourceAdvisor
 *          1）事务增强器要用事务注解的信息，AnnotationTransactionAttributeSource 解析事务注解
 *          2）事务拦截器，TransactionInterceptor 保存了事务属性信息，事务管理器，
 *             它是一个MethodInterceptor；
 *             在目标方法执行的时候，
 *                 执行拦截器链
 *                 事务拦截器
 *                     1）获取事务相关的属性
 *                     2）再获取 PlatformTransactionManager ，如果事先没有添加指定任何trasactionManager没，最终
 *                        会从容器中按照类型获取一个PlatformTransactionManager
 *                     3）执行目标方法
 *                        如果异常，获取到事务管理器，利用事务管理器回滚操作；
 *                        如果正常，获取到事务管理器，利用事务管理器提交事务；
 *
 * 【总结】
 *  整个流程跟AOP类似
 *
 */
@EnableTransactionManagement
@ComponentScan("com.test_shangguigu.t31_tx")
@Configuration
public class AppConfig31 {

	@Bean
	public DataSource dataSource() throws  Exception {
		ComboPooledDataSource ds = new ComboPooledDataSource();
		ds.setUser("root");
		ds.setPassword("11111111"); //8个1
		ds.setDriverClass("com.mysql.jdbc.Driver");
		ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/atest");
		return ds;
	}

	//
	@Bean
	public JdbcTemplate jdbcTemplate() throws Exception {
		// Spring对 @Configuration 类会特殊处理，给容器中添加组件的放啊发，多次调用只是从容器中找组件。
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource()); // 注意：加了@Bean，dataSource()调用是从容器中找组件。
		return jdbcTemplate;
	}

	// 事务管理器
	@Bean
	public PlatformTransactionManager transactionManager() throws Exception {
		return new DataSourceTransactionManager(dataSource());
	}




























}
