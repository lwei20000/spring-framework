package com.kittycoder.aoptest;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.ThrowsAdvice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;

/**
 * @Auther: weiliang
 * @Date: 2020/12/14 22:35
 * @Description:
 */
public class CountingThrowsAdvice extends MethodCounter implements ThrowsAdvice {
	public void afterThrowing(IOException exception) {
		count(IOException.class.getName());
	}
	public void afterThrowing(UncheckedIOException uncheckedIOException) {
		count(UncheckedIOException.class.getName());
	}
}
