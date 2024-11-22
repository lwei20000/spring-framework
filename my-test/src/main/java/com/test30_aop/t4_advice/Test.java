package com.test30_aop.t4_advice;

import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.RegexpMethodPointcutAdvisor;

/**
 * 给目标对象ITargetImpl的say()方法加上两个通知(方法调用时通知，方法返回时的通知)
 * @Auther: weiliang
 * @Date: 2020/12/15 10:01
 * @Description:
 */
public class Test {
	public static void main(String[] args) {
		// 创建ProxyFactory
		ITarget target = new ITargetImpl();
		ProxyFactory pf = new ProxyFactory(target);

		// 新建两个通知，表明在什么时机切入
		Advice afterReturningAdvice = new MyAfterReturningAdvice();
		Advice beforeAdvice = new MyBeforeAdvice();

		// Advisor包含Pointcut和Advice，叫通知器，这里就设置了切点pointcut
		RegexpMethodPointcutAdvisor regexpAdvisor1 = new RegexpMethodPointcutAdvisor();
		regexpAdvisor1.setPattern("com.test30_aop.t4_advice.ITargetImpl.say()");
		regexpAdvisor1.setAdvice(afterReturningAdvice);

		RegexpMethodPointcutAdvisor regexpAdvisor2 = new RegexpMethodPointcutAdvisor();
		regexpAdvisor2.setPattern("com.test30_aop.t4_advice.ITargetImpl.say()");
		regexpAdvisor2.setAdvice(beforeAdvice);

		// 将通知器Advisor注册到ProxyFactory
		pf.addAdvisor(regexpAdvisor1);
		pf.addAdvisor(regexpAdvisor2);

		// 生成代理，执行方法
		ITarget proxy = (ITarget) pf.getProxy();
		proxy.say();
	}
}
