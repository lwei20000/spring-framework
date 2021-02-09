package com.test_shangguigu.t07_lifecircle;


import com.ACUtils;
import com.test_shangguigu.t07_lifecircle.config.AppConfig07;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class IocTest {
	public static void main(String[] args) {

		// 1.创建ioc容器
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig07.class);
		System.out.println("容器创建完成。。。");

		ACUtils.printAllBeans(ac); // 打印所有bean
		Object bean1 = ac.getBean("car");

		// 2.关闭容器，bean的生命周期会调用销毁方法进行销毁。
		ac.close();

	}
}