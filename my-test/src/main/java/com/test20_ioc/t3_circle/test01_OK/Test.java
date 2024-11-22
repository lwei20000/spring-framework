package com.test20_ioc.t3_circle.test01_OK;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 容器处理了的循环依赖
 */
public class Test {

	public static void main(String[] args) {

		ApplicationContext ac = new ClassPathXmlApplicationContext("com/test20_ioc_circle_OK.xml");
		TestA a = (TestA) ac.getBean("testA");
	}
}