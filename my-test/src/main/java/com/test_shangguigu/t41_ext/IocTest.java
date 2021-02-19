package com.test_shangguigu.t41_ext;


import com.test_shangguigu.t41_ext.config.AppConfig41;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class IocTest {
	public static void main(String[] args) throws Exception {

		// 创建ioc容器
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig41.class);
		// ACUtils.printAllBeans(ac); // 打印所有bean

		// 发布一个事件
		ac.publishEvent(new ApplicationEvent(new String("我发布的事件")) {
			private static final long serialVersionUID = 7099057708183571930L;
		});

		ac.close();
	}
}