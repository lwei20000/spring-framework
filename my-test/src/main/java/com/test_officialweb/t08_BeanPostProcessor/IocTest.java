package com.test_officialweb.t08_BeanPostProcessor;

import com.test_officialweb.t08_BeanPostProcessor.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 */
public class IocTest {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
	}
}