package com.kittycoder.aoptest;

import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * @Auther: weiliang
 * @Date: 2020/12/15 10:02
 * @Description:
 */
public class MyAfterReturningAdvice implements AfterReturningAdvice {
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		System.out.println("afterReturning say");
	}
}
