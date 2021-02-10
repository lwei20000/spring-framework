package com.test_shangguigu.t21_aop.config;

import com.test_shangguigu.t06_import.beans.Yellow;
import com.test_shangguigu.t21_aop.LogAspect;
import com.test_shangguigu.t21_aop.MathCalculator;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.*;
import org.springframework.util.StringValueResolver;

/**
 * AOP【动态代理】
 *     指在程序运行期间动态将某段代码加入到指定方法指定位置进行运行的编码方式；
 *
 * 1 导入AOP模块：Spring AOP：（spring-aspects）
 * 2 定义一个业务逻辑类（MathCalculator）：在业务逻辑运行的时候将日志进行答应（方法运行之前，运行之后，出现异常）
 * 3 定义一个日志切面类（LogAspect），切面类里面的方法需要动态感知div运行到哪里了然后执行对应的方法
 *         通知方法：
 *             前置通知(@Before) logStart 在目标方法运行之前运行
 *             后置通知(@After) logEnd 在目标方法运行之后运行
 *             返回通知(@AfterReturing) logReturn 在目标方法正常返回之后运行
 *             异常通知(@AferThrowing) logException 在目标方法运行异常以后运行
 *             环绕通知(@Around) 动态代理，手动推进目标方法运行（JoinPoint.proceed()）
 * 4 给切面类的目标方法标注合适何地运行（通知注解）
 * 5 将切面类和业务逻辑类（目标方法所在类）都加入到容器中；
 * 6 必须告诉Spring哪个类是切面类 @Aspect修饰切面类
 *【7】开启aspectj
 *    xml时代：<aop:aspectj-autoproxy></aop:aspectj-autoproxy>
 *    ann时代：@EnableAspectJAutoProxy
 *
 * 在Spring中，未来会有很多
 */
@EnableAspectJAutoProxy
@Configuration
public class AppConfig21 {

	// 业务逻辑类加入到容器中
	@Bean
	public MathCalculator calculator() {
		return new MathCalculator();
	}

	// 切面类加入到容器中
	@Bean
	public LogAspect logAspect() {
		return new LogAspect();
	}

}
