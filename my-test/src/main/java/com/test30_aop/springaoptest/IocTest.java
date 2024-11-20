package com.test30_aop.springaoptest;

import org.aopalliance.aop.Advice;
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

		// 新建两个通知，表明在什么时机切入
		Advice mySecurityInterceptor = new MySecurityInterceptor();
		Advice myLoggerBeforeAdvice = new MyLoggerBeforeAdvice();

		//3.装配Advice
		pf.addAdvice(mySecurityInterceptor);
		pf.addAdvice(myLoggerBeforeAdvice);

		//4.装配advisor
		pf.addAdvisor(new DefaultPointcutAdvisor(new MyLoggerBeforeAdvice()));

		//4.获取代理对象，调用业务
		UserService proxy =(UserService)pf.getProxy();
		proxy.updateUser();
	}
}