package com.test20_ioc.t3_circle.test02_NG;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 容器无法处理的循环依赖
 */
public class Test {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
		System.out.println(ac.getBean("aService"));
		System.out.println(ac.getBean("bService"));
	}
}
