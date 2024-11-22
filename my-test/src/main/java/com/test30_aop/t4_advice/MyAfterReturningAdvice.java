package com.test30_aop.t4_advice;

import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * 通知advice：在方法返回时的通知
 * @Auther: weiliang
 * @Date: 2020/12/15 10:02
 * @Description:
 */
public class MyAfterReturningAdvice implements AfterReturningAdvice {
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		System.out.println("在方法返回时的通知 say");
	}
}
