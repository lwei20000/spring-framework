package com.test_ioc.xml;

import com.beans.Teacher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 梁威测试文件(lw)
 */
public class IocTest {

	public static void main(String[] args) {

		// =============================================================================================================
		// 案例一：容器的功能扩展
		// =============================================================================================================
		// 【一、读取配置文件，生成bean工厂】
		// ClassPathXmlApplicationContext
		// -->AbstractXmlApplicationContext
		// -->AbstractRefreshableConfigApplicationContext
		// -->AbstractRefreshableApplicationContext
		// -->AbstractApplicationContext(大类)
		// -->DefaultResourceLoader
		// -->ResourceLoader(接口)
		ApplicationContext ac = new ClassPathXmlApplicationContext("com/spring-student.xml");
		// 【二、从ApplicationContext中取得bean】
		Teacher teacher = (Teacher) ac.getBean("teacher");
		System.out.println("=====================" + teacher);




		// =============================================================================================================
		// 案例二：容器的基本实现
		// =============================================================================================================
//		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("beanFactoryTest.xml"));
//		Teacher teacher2 = (Teacher) bf.getBean("teacher");
//		System.out.println("=====================" + teacher2);

	}
}