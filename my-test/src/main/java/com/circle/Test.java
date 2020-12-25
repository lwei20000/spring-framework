package com.circle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Auther: weiliang
 * @Date: 2020/12/23 15:28
 * @Description:
 */
public class Test {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
		System.out.println(ac.getBean("aService"));
		System.out.println(ac.getBean("bService"));
	}
}
