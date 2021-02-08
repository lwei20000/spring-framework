package com.test_shangguigu.t03_scope;


import com.ACUtils;
import com.test_shangguigu.t03_scope.config.AppConfig03;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IocTest {
	public static void main(String[] args) {
		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig03.class);

		ACUtils.printAllBeans(ac); // 打印所有bean

		Object bean1 = ac.getBean("person");
		Object bean2 = ac.getBean("person");
		System.out.println(bean1==bean2);
	}
}