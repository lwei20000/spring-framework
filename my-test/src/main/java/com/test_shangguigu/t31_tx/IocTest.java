package com.test_shangguigu.t31_tx;


import com.ACUtils;
import com.test_shangguigu.t31_tx.config.AppConfig31;
import com.test_shangguigu.t31_tx.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class IocTest {
	public static void main(String[] args) throws Exception {

		// 创建ioc容器
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig31.class);
		ACUtils.printAllBeans(ac); // 打印所有bean

		UserService userService = ac.getBean(UserService.class);
		userService.insertUser();


	}
}