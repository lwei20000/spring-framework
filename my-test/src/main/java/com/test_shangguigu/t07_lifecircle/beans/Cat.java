package com.test_shangguigu.t07_lifecircle.beans;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class Cat implements InitializingBean, DisposableBean {

	public Cat() {
		System.out.println("cat construction...");
	}

	@Override
	public void destroy() throws Exception {
		System.out.println("cat destroy...");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("cat afterPropertiesSet...");
	}
}
