package com.test_shangguigu.t00_component_scan;

import com.ACUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public class IocTest {
	public static void main(String[] args) {
		ApplicationContext ac = new ClassPathXmlApplicationContext("com/spring-t00_component_scan.xml");
		ACUtils.printAllBeans(ac);
	}
}