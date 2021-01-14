package com.test_ioc.ann;

import com.test_ioc.ann.config.AppConfig;
import com.test_ioc.ann.service.PayService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 测试：AnnotationConfigApplicationContext
 */
public class IocTest {

	public static void main(String[] args) {
		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext context= new AnnotationConfigApplicationContext(AppConfig.class);
		PayService user = (PayService)context.getBean("payService");
		System.out.println(user);
	}
}