package com.test_shangguigu.t01_configuration;


import com.test_shangguigu.t01_configuration.beans.Persion;
import com.test_shangguigu.t01_configuration.config.AppConfig01;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IocTest {
	public static void main(String[] args) {
		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig01.class);
		Persion persion = (Persion)ac.getBean("persion");
		System.out.println(persion);
	}
}