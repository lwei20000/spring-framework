package com.test12_circle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 循环依赖的例子
 */
public class Test {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
		System.out.println(ac.getBean("aService"));
		System.out.println(ac.getBean("bService"));
	}
}
