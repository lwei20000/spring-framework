package com.test20_ioc.xml;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 梁威测试文件(lw)
 */
public class Test {

	public static void main(String[] args) {

		// =============================================================================================================
		// 案例一：容器的功能扩展
		// =============================================================================================================
		ApplicationContext ac = new ClassPathXmlApplicationContext("com/test20_ioc_xml.xml");
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
//		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("com/test20_ioc_xml.xml"));
//		Teacher teacher2 = (Teacher) bf.getBean("teacher");
//		System.out.println("=====================" + teacher2);

	}
}