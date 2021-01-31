package com.test_officialweb.t07_FactoryBean;

import com.ACUtils;
import com.test_officialweb.t07_FactoryBean.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;

/**
 *
 */
public class IocTest {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext ac= new AnnotationConfigApplicationContext(AppConfig.class);
		System.out.println("直接调用getBean(\"myFactoryBean\")返回："+ac.getBean("myFactoryBean"));
		System.out.println("调用getBean(\"&myFactoryBean\")返回："+ac.getBean("&myFactoryBean"));

		// 查看xml方式生成的ioc容器中，默认添加的bean
		// 对比annotation的方式，xml方式只有文件中配置的bean。没有多余的后置处理器。
		ACUtils.printAllBeans(ac);
	}
}