package com.test_spring.add;

import com.beans.Cat;
import com.test_ioc.ann.config.AppConfig;
import com.beans.PayService;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 */
public class IocTest {

	public static void main(String[] args) {

		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext context= new AnnotationConfigApplicationContext(AppConfig.class);
		PayService user = (PayService)context.getBean("payService");
		System.out.println(user);

		// 往context中添加一个bean---cat
		DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
		RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(Cat.class);
		beanFactory.registerBeanDefinition("cat", rootBeanDefinition);

		// 打印cat
		Cat cat = (Cat)context.getBean("cat");
		System.out.println(cat);
	}
}