package com.test21_aop.normal;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @Auther: weiliang
 * @Date: 2020/12/26 12:17
 * @Description:
 */
public class HijackAroundMethod implements MethodInterceptor {
		@Override
		public Object invoke(MethodInvocation methodInvocation) throws Throwable {

//			System.out.println("Method name : " + methodInvocation.getMethod().getName());
//			System.out.println("Method arguments : " + Arrays.toString(methodInvocation.getArguments()));

			System.out.println("HijackAroundMethod : Before method hijacked!");
			Object result = methodInvocation.proceed();
			System.out.println("HijackAroundMethod : After method hijacked!");
			return result;

		}
	}
