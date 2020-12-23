//package com.circle.aop;
//
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.springframework.context.annotation.EnableAspectJAutoProxy;
//
///**
// * @Auther: weiliang
// * @Date: 2020/12/23 16:21
// * @Description:
// */
//@Aspect
//public class LubanAspect {
//
//	@Around("execution(* com.circle.service)")
//	public void invoke(ProceedingJointPoint point) {
//		System.out.println("aop");
//		point.proceed();
//	}
//}
