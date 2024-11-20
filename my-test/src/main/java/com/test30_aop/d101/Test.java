package com.test30_aop.d101;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

import java.lang.reflect.Method;

/**
 * 《Spring技术内幕》P137的ProxyFactory的测试小例子
 */
public class Test {
	public static void main(String[] args) {
		TargetImpl target = new TargetImpl();

		ProxyFactory aopFactory = new ProxyFactory(target);

		MethodBeforeAdvice yourAdvice = new MethodBeforeAdvice() {
			@Override
			public void before(Method method, Object[] args, Object target) throws Throwable {
				// method表示当前执行的方法,args表示执行方法的参数,target表示target对象
				System.out.println("执行目标方法调用之前的逻辑");
			}
		};
		DefaultPointcutAdvisor yourAdvisor = new DefaultPointcutAdvisor(yourAdvice);

		aopFactory.addAdvisor(yourAdvisor);

		aopFactory.addAdvice(yourAdvice);

		TargetImpl targetProxy = (TargetImpl) aopFactory.getProxy();
		targetProxy.test();

	}
}
