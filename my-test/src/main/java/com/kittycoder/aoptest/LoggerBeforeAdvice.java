package com.kittycoder.aoptest;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @Auther: weiliang
 * @Date: 2020/12/14 10:09
 * @Description:
 */
public class LoggerBeforeAdvice implements MethodBeforeAdvice {
	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		System.out.println("=======保存更新日志=========");
	}
}
