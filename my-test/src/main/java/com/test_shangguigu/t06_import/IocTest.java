package com.test_shangguigu.t06_import;


import com.ACUtils;
import com.test_shangguigu.t06_import.config.AppConfig06;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;


public class IocTest {
	public static void main(String[] args) {

		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig06.class);

		ACUtils.printAllBeans(ac); // 打印所有bean
		Object bean1 = ac.getBean("person");
	}
}