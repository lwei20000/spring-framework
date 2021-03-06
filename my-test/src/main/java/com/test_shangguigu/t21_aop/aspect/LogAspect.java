package com.test_shangguigu.t21_aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

/**
 * 日志切面类
 */
@Aspect
public class LogAspect {

	// 抽取公共的切入点表达式
	// 1、本类应用
	// 2、其他的切面引用
	@Pointcut("execution(public int com.test_shangguigu.t21_aop.MathCalculator.*(..))")
	public void pointCut(){} //不用定义方法实体

	// @Before---在目标方法之前切入了；pointCut()---切入点表达式（指定在哪个方法切入）
	@Before("pointCut()")
	public void logStart() {
		System.out.println("除法运行。。。参数列表是：{}");
	}

	@After("com.test_shangguigu.t21_aop.aspect.LogAspect.pointCut()")
	public void logEnd() {
		System.out.println("除法结束。。。");
	}

	@AfterReturning(value="pointCut()",returning="result")
	public void logReturn(JoinPoint joinPoint, Object result) { // jointPoint参数如果要使用它，必须放在第一个参数位置，否则Spring识别不了
		System.out.println(joinPoint.getSignature().getName() + "除法正常返回。。。运行结果：" + result);
	}

	@AfterThrowing(value="pointCut()",throwing="exception")
	public void logException(Exception exception) {
		System.out.println("除法异常。。。异常结果：「」" + exception.getMessage());
	}
}
