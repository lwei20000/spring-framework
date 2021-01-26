package com.test_ioc.xml;

import com.beans.Teacher;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * 梁威测试文件(lw)
 */
public class IocTest {

	public static void main(String[] args) {

		// =============================================================================================================
		// 案例一：容器的功能扩展
		// =============================================================================================================
		ApplicationContext ac = new ClassPathXmlApplicationContext("com/spring-student.xml");
		// 【二、从ApplicationContext中取得bean】
		Teacher teacher = (Teacher) ac.getBean("teacher");
		System.out.println("=====================" + teacher);


		// 查看xml方式生成的ioc容器中，默认添加的bean
		// 对比annotation的方式，xml方式只有文件
		String[] beanDefinitionNames = ac.getBeanDefinitionNames();
		for(String beanDefinitionName : beanDefinitionNames){
			System.out.println(beanDefinitionName);
		}


		// =============================================================================================================
		// 案例二：容器的基本实现
		// =============================================================================================================
//		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("com/spring-student.xml"));
//		Teacher teacher2 = (Teacher) bf.getBean("teacher");
//		System.out.println("=====================" + teacher2);

	}
}