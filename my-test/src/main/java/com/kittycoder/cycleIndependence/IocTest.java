package com.kittycoder.cycleIndependence;

import com.kittycoder.simpledemo.po.Teacher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 循环依赖的测试
 */
public class IocTest {

	public static void main(String[] args) {
		ApplicationContext ac = new ClassPathXmlApplicationContext("com/kittycoder/simpledemo/spring-student.xml");
		// 【二、从ApplicationContext中取得bean】
		Teacher teacher = (Teacher) ac.getBean("teacher");
		System.out.println("=====================" + teacher);
	}
}