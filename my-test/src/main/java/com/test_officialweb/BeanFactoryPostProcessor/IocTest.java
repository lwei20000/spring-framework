package com.test_officialweb.BeanFactoryPostProcessor;

import com.beans.Teacher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;

/**
 * Spring官网阅读（六）容器的扩展点（一）BeanFactoryPostProcessor
 *
 * 处理配置文件中数据源的占位符
 * 	<bean id="dataSource" destroy-method="close" class="org.apache.commons.dbcp.BasicDataSource">
 * 		<property name="driverClassName" value="${jdbc.driverClassName}"/>
 * 		<property name="url" value="${jdbc.url}"/>
 * 		<property name="username" value="${jdbc.username}"/>
 * 		<property name="password" value="${jdbc.password}"/>
 * 	</bean>
 */
public class IocTest {

	public static void main(String[] args) {

		ApplicationContext ac = new ClassPathXmlApplicationContext("com/spring-beanFactoryPostProcessor.xml");
		DataSource dataSource = (DataSource) ac.getBean("dataSource");
		System.out.println("=====================" + dataSource);

		// 查看xml方式生成的ioc容器中，默认添加的bean
		// 对比annotation的方式，xml方式只有文件中配置的bean。没有多余的后置处理器。
		String[] beanDefinitionNames = ac.getBeanDefinitionNames();
		for(String beanDefinitionName : beanDefinitionNames){
			System.out.println(beanDefinitionName);
		}
	}
}