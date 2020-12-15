package com.kittycoder.aoptest;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @Auther: weiliang
 * @Date: 2020/12/14 22:35
 * @Description:
 */
public class CountingAfterReturningAdvice extends MethodCounter implements AfterReturningAdvice {
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		count(method);
	}
}
