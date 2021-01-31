package com.test_shangguigu.t01_configuration;


import com.test_shangguigu.t01_configuration.beans.Persion;
import com.test_shangguigu.t01_configuration.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IocTest {
	public static void main(String[] args) {
		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
		Persion persion = (Persion)ac.getBean("persion");
		System.out.println(persion);
	}
}