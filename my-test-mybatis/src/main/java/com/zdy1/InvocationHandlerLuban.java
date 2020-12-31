package com.zdy1;

import org.apache.ibatis.annotations.Select;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 21:19
 * @Description:
 */
public class InvocationHandlerLuban implements InvocationHandler {

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.getName().equals("toString")) {
			return proxy.getClass().getName();
		}
		System.out.println("假装jdbc 连接");
		System.out.println(method.getAnnotation(Select.class).value()[0]);
		System.out.println("假装执行sql" );
		return null;
	}
}













