package com.test_shangguigu.t05_conditional;


import com.ACUtils;
import com.test_shangguigu.t05_conditional.config.AppConfig05;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;


public class IocTest {
	public static void main(String[] args) {


		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig05.class);


		ConfigurableEnvironment env = ac.getEnvironment();
		String osname = env.getProperty("os.name");  //Mac OS X
		System.out.println(osname);


		ACUtils.printAllBeans(ac); // 打印所有bean
		//Object bean1 = ac.getBean("person");
	}
}