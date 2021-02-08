package com.test_shangguigu.t04_lazyBean;


import com.ACUtils;
import com.test_shangguigu.t04_lazyBean.config.AppConfig04;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IocTest {
	public static void main(String[] args) {
		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig04.class);
		ACUtils.printAllBeans(ac); // 打印所有bean
		Object bean1 = ac.getBean("person");
	}
}