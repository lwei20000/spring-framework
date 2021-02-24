package com.test_officialweb.t01_instantiate;

import com.ACUtils;
import com.beans.Bird;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 */
public class IocTest {

	public static void main(String[] args) {

		// AnnotationConfigApplicationContext是GenericApplicationContext的一个子类
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
		// 双冒号::是java8引入的新特性---方法引用（method reference）。
		// 方法引用是与lambda表达式相关的一个重要特性。它提供了一种不执行方法的方法。
		ac.registerBean("bird", Bird.class, Bird::new);
		ac.refresh();
		System.out.println(ac.getBean("bird"));

		// 查看xml方式生成的ioc容器中，默认添加的bean
		// 对比annotation的方式，xml方式只有文件中配置的bean。没有多余的后置处理器。
		ACUtils.printAllBeans(ac);
	}
}