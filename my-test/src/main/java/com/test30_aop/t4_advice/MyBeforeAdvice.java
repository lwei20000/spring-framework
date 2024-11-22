package com.test30_aop.t4_advice;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * 通知advice：在方法调用之前时的通知
 * @Auther: weiliang
 * @Date: 2020/12/15 10:03
 * @Description:
 */
public class MyBeforeAdvice implements MethodBeforeAdvice {
	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		System.out.println("在方法调用之前时的通知 say");
	}
}
