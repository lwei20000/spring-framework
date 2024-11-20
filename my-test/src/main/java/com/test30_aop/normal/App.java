package com.test30_aop.normal;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Auther: weiliang
 * @Date: 2020/12/26 12:18
 * @Description:System.out.println("*************************");
 */
public class App {
	public static void main(String[] args) {
		ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:com/aop/normal/Spring-Customer.xml" );
		CustomerService cs = (CustomerService) ac.getBean("customerServiceProxy");

		System.out.println("*************************");
		cs.printName();
		System.out.println("*************************");
		cs.printURL();
		System.out.println("*************************");
		try {
			cs.printThrowException();
		} catch (Exception e) {
		}
		System.out.println("*************************");
	}
}
