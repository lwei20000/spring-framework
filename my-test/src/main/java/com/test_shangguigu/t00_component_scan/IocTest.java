package com.test_shangguigu.t00_component_scan;

import com.ACUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 尚硅谷Spring注解驱动教程(雷丰阳源码级讲解)
 * https://www.bilibili.com/video/BV1gW411W7wy
 */
public class IocTest {
	public static void main(String[] args) {
		ApplicationContext ac = new ClassPathXmlApplicationContext("com/spring-t00_component_scan.xml");
		ACUtils.printAllBeans(ac);
	}
}