package com.aop;

import org.springframework.stereotype.Component;

/**
 * 目标对象，需要被代理的类及方法
 * Created by Kevin on 2017/11/15.
 */
@Component("testPoint")
public class TestPoint {

	public void test() {
		System.out.println("========目标方法调用");
	}
}
