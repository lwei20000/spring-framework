package com.kittycoder.springaoptest;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * 梁威测试文件(lw)
 */
public class IocTest {

	public static void main(String[] args) {
		//1.初始化源对象(一定要实现接口)
		UserService target = new UserServiceImpl();

		//2.AOP 代理工厂
		ProxyFactory pf = new ProxyFactory(target);

		//3.装配Advice
		pf.addAdvice(new MySecurityInterceptor());
		pf.addAdvice(new MyLoggerBeforeAdvice());

		//4.装配advisor
		//pf.addAdvisor(new DefaultPointcutAdvisor(new MyLoggerBeforeAdvice()));

		//4.获取代理对象，调用业务
		UserService proxy =(UserService)pf.getProxy();
		proxy.updateUser();
	}
}