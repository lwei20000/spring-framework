package com.test_shangguigu.t10_profile;


import com.ACUtils;
import com.test_shangguigu.t10_profile.config.AppConfig10;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class IocTest {
	public static void main(String[] args) {

		// 创建ioc容器
		//AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig10.class);

		// 创建一个applicationcontext
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();

		// 设置需要激活的环境
		ac.getEnvironment().setActiveProfiles("test","dev");

		ACUtils.printAllBeans(ac); // 打印所有bean

		// 注册主配置类
		ac.register(AppConfig10.class);

		// 启动刷新容器
		ac.refresh();

		ACUtils.printAllBeans(ac); // 打印所有bean

		ac.close();
	}
}