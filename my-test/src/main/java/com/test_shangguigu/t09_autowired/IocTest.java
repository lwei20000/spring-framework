package com.test_shangguigu.t09_autowired;


import com.ACUtils;
import com.test_shangguigu.t09_autowired.beans.Person;
import com.test_shangguigu.t09_autowired.config.AppConfig09;
import com.test_shangguigu.t09_autowired.service.BookService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class IocTest {
	public static void main(String[] args) {

		// 创建ioc容器
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig09.class);
		ACUtils.printAllBeans(ac); // 打印所有bean

		BookService bean = ac.getBean(BookService.class);
		System.out.println(bean);

		ac.close();
	}
}