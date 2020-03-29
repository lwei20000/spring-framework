package com.kittycoder.simpledemo;

import com.kittycoder.simpledemo.po.Teacher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 梁威测试文件(lw)
 */
public class SimpleDemoTest {

	public static void main(String[] args) {
		ApplicationContext ac = new ClassPathXmlApplicationContext("com/kittycoder/simpledemo/spring-student.xml");
		Teacher teacher = (Teacher) ac.getBean("teacher");
		System.out.println("=====================" + teacher);

//		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("beanFactoryTest.xml"));
//		Teacher teacher = (Teacher) bf.getBean("teacher");
//		System.out.println("=====================" + teacher);

	}
}