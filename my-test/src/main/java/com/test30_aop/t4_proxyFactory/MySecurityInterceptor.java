package com.test30_aop.t4_proxyFactory;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @Auther: weiliang
 * @Date: 2020/12/14 10:09
 * @Description:
 */
public class MySecurityInterceptor implements MethodInterceptor {
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		System.out.println("==========执行安全校验====================");
		return methodInvocation.proceed();
	}
}
