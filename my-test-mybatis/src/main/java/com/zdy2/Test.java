package com.zdy2;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 我们自己通过动态代理，模拟mybatis的过程
 * SqlSessionLuban中通过Proxy.newInstance生成动态代理类。
 */
public class Test {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
		ac.getBean(CityService.class).queryAll();
	}
}
