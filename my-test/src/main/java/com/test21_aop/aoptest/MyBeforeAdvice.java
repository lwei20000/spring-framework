package com.test21_aop.aoptest;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @Auther: weiliang
 * @Date: 2020/12/15 10:03
 * @Description:
 */
public class MyBeforeAdvice implements MethodBeforeAdvice {
	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		System.out.println("before say");
	}
}
