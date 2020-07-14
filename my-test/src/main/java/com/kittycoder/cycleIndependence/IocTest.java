package com.kittycoder.cycleIndependence;

import com.kittycoder.cycleIndependence.config.AppConfig;
import com.kittycoder.cycleIndependence.service.PayService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 */
public class IocTest {

	public static void main(String[] args) {


		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext context= new AnnotationConfigApplicationContext(AppConfig.class);
		PayService user = (PayService)context.getBean("payService");
		System.out.println(user);







		//DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
		//RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(cat.class);
		//beanFactory.registerBeanDefinition("cat", rootBeanDefinition);
	}
}