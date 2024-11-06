package com.test01_java.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Auther: weiliang
 * @Date: 2020/12/25 14:53
 * @Description:
 */
public class MyStoryInvocationHandler implements InvocationHandler {

	private Object object;

	public MyStoryInvocationHandler(Object o) {
		this.object = o;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		// 前置业务
		System.out.println("前置业务");

		// 通过反射机制，通知力宏做事情
		method.invoke(object, args);

		// 后置业务
		System.out.println("后置业务");

		return null;
	}
}
