package com.test_shangguigu.t02_componentScan;


import com.ACUtils;
import com.test_shangguigu.t02_componentScan.config.AppConfig02;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IocTest {
	public static void main(String[] args) {
		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig02.class);
		ACUtils.printAllBeans(ac);

	}
}