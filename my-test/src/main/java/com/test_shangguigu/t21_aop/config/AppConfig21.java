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
 *    ann时代：@EnableAspectJAutoProxy   （在Spring中有很多的@Enablexxx）
 *
 * 归纳起来总共分为三步：
 *  1）将业务逻辑组件和切面都加入到容器中，并告诉spring哪个是切面类
 *  2）在切面类上的每一个通知方法上标注通知注解，告诉spring何时何地运行（切入点表达式）
 *  3）开启基于注解的aop模式@EnableAspectJAutoProxy
 *
 * AOP的原理：【基本原则：看给容器中注册了什么组件，这个组件什么时候工作，以及他的功能是什么】
 * 1 @EnableAspectJAutoProxy
 *        @Import(AspectJAutoProxyRegistrar.class) 给容器中导入了AspectJAutoProxyRegistrar
 *            利用AspectJAutoProxyRegistrar自定义给容器中注册bean
 *            internalAutoProxyCreator=AnnotationAwareAspectJAutoProxyCreator
 *        给容器中注册了一个 AnnotationAwareAspectJAutoProxyCreator（注解装配模式的AspectJ自动代理创建器）
 * 2 AnnotationAwareAspectJAutoProxyCreator --看下面的继承关系，它实际上是一个后置处理器
 *     --->AspectJAwareAdvisorAutoProxyCreator
 *         --->AbstractAdvisorAutoProxyCreator
 *             --->AbstractAutoProxyCreator
 *                --->SmartInstantiationAwareBeanPostProcessor （后置处理器） , BeanFactoryAware （Aware接口）
 *                    核心关注后置处理器（在bean初始化完成之后做事情）、核心关注自动装配BeanFactory
 *
 *  【断点】
 *   AbstractAutoProxyCreator.setBeanFactory()
 *   AbstractAutoProxyCreator.后置处理器对应的方法
 *
 *   AbstractAdvisorAutoProxyCreator.setBeanFactory() -> initBeanFactoru()
 *
 *   AnnotationAwareAspectJAutoProxyCreator.initBeanFactory()
 *
 *   【流程】
 *    1、传入配置类，创建ioc容器
 *    2、注册配置类，调用refresh()刷新容器
 *    3、registerBeanPostProcessors(beanFactory)，注册bean的后置处理器来方便拦截bean的创建：
 *         1）先获取ioc容器中已经定义了的需要创建对象的所有BeanPostProcessor （容器中本来就有一些我们要用的后置处理器，以及配置类中会创建的后置处理器）
 *         2）给容器中加别的BeanPostProcessor
 *         3）优先注册实现了PriorityOrdered接口的BeanPostProcessor
 *         4）在给容器中注册实现了Ordered接口的BeanPostProcessor
 *         5）注册实现优先级接口的BeanPostProcessor
 *         6）注册BeanPostProcessor，实际上就是创建BeanPostProcessor对象，保存在容器中；
 *            创建InternalAutoProxyCreator的BeanPostProcessor ===> AnnotationAwareAspectJAutoProxyCreator
 *            1）创建Bean的实例
 *            2）populationBean，给bean的各种属性赋值
 *            3）initializeBean：初始化bean：
 *               1）invokeAwareMathods：判断bean对象是否是Aware接口，也即处理Aware接口。
 *               2）applyBeanPostProcessorBeforeInitialization()：应用后置处理器的对应的Before方法
 *               3）invokeInitMethods()：执行自定义的初始化方法
 *               4）applyBeanPostProcessorAfterInitialization：应用后置处理器的对应的After方法
 *            4）BeanPostProcessor(AnnotationAwareAspectJAutoProxyCreator)创建成功；--->
 *         7）把BeanPostProcessor注册到BeanFactory中；
 *            beanFactory.addBeanPostProcessor(postProcessor);
 *         ================= 以上是创建和注册 AnnotationAwareAspectJAutoProxyCreator 的过程 =================
 *         AnnotationAwareAspectJAutoProxyCreator =》 InstantiationAwareBeanPostProcessor
 *    4、finishBeanFactoryInitialization(beanFactory); 完成BeanFactory初始化工作：创建剩下的单实例bean。
 *        1）遍历获取容器中所有的Bean，一次创建对象getBean(beanName);
 *           getBean() -- doGetBean() -- getSingleton()
 *        2) 创建bean
 *           1）先从缓存中获取当前bean，如果能获取到，说明bean是之前被创建过的，直接使用；否则再创建
 *              只要创建好的bean都会被缓存起来。
 *           2）createBean()； 创建bean ：AnnotationAwareAspectJAutoProxyCreator 会在任何bean创建之前尝试返回bean的实例
 *             【后置处理器跟后置处理器不一样：】
 *             【BeanPostProcessor是在Bean对象创建完成初始化前后调用的 =============================== postProcessBeforeInitialization  】
 *             【InstantiationAwareBeanPostProcessor是在创建Bean实例之前先尝试用后置处理器返回对象的 === postProcessBeforeInstantiation 】
 *              1）resolveBeforeInstantiation(beanName, mbdToUse);解析BeforeInstantiation
 *                 希望后置处理器在次能返回一个代理对象，如果能返回代理对象就使用，如果不能就继续
 *              2）doCreateBean(beanName, mbdToUse, args);真正的去创建一个bean实例；和3.6的流程完全是一样的。
 *
 *
 *
 *    AnnotationAwareAspectJAutoProxyCreator【InstantiationAwareBeanPostProcessor】的作用
 *    1）每一个bean创建之前，调用postProcessBeforeinstantiation()
 *        关心 MathCalculator 和 LogAspect 的创建
 *        1）判断当前bean是否在advicedBeans中（保存了所有需要增强的bean）
 *        2）判断当前bean是否是基础类型的（Advice、PointCut、AopInfrastructureBean）
 *        3）是否可以跳过skip
 *            1）获取候选的增强其（切面里面的增强方法）「List<Advisor> candidateAdvisors」
 *               每一个封装的通知方法的增强器是InstantiationModelAwarePointcutAdvisor；
 *               判断每一个增强器是否是AspectJPointcutAdvisor类型的；
 *            2）永远返回false
 *    2）创建对象之后，调用postProcessBeforeinstantiation()；
 *        return wrapIfNecessary(bean, beanName, cachekey); // 包装如果需要的情况下
 *        什么情况下需要包装呢？
 *            Create proxy if we hava advice
 *        1)获取当前bean的所有增强器（通知方法） Object[] specificInterceptors
 *            1）找到候选的所有增强器（找哪些通知方法是需要切入当前bean方法的）
 *            2）获取到能在当前bean使用的增强器
 *            3）给增强器排序
 *        2）保存当前bean在advisedBeans中；
 *        3）如果当前bean需要增强，创建当前bean的代理对象
 *            1）获取所有增强器（通知方法）
 *            2）保存到proxyFactory
 *            3）创建代理对象
 *                JdkDynamicAopProxy(config); jdk动态代理
 *                ObjenesisCglibAopProxy(config); cglib的动态代理；
 *        4）给容器中返回当前组件使用cglib增强了的代理对象
 *        5）以后容器中获取到的就是这个组件的代理对象，执行目标方法的时候，代理对象就会执行通知方法的流程；
 *
 *    3）目标方法执行
 *        容器中保存了组件的代理对象（cglib增强后的对象），这个对象里面保存了详细信息（增强器、目标对象，xxx）
 *        1）CglibAopProxy.intercept(); 拦截目标的执行
 *        2）根据ProxyFactory对象获取将要执行的目标方法拦截器链；
 *            List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
 *            1）List<Object> interceptorList保存所有拦截器 5
 *               一个默认的 ExposeInvocationInterceptor 和 4个增强器；
 *            2）遍历所有的增强器，将其转为interceptor；
 *               registry.getInterceptors(advisor);
 *            3) 将增强器转为List<MethodInterceptor>;
 *               如果是MethodInterceptor，直接加入到集合中
 *               如果不是，使用AdvisorAdapter将增强器转为 MethodInterceptor
 *               转换完成返回MethodInterceptor数组；
 *        3）如果没有拦截器链，直接执行目标方法；
 *            拦截器链（每一个通知方法又被包装为方法拦截器，利用MethodInterceptor机制）
 *        4）如果有拦截器链，把需要执行的目标对象、目标方法、
 *           拦截器链等信息传入创建一个CglibMethodInvocation对象，
 *           并调用其 Object retVal = mi.proceed;
 *        5）拦截器链的触发过程;
 *           1) 如果没有拦截器，执行目标方法，或者拦截器的索引和拦截器数组-1大小一样（指定到了最后一个拦截器）执行目标方法；
 *           2）链式获取每一个拦截器，拦截器执行invoke方法，每一个拦截器等待下一个拦截器执行完成返回以后再来执行；
 *              拦截器链的机制，来保证通知方法与目标方法的执行顺序：
 *              （this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1）记录下标。
 *              mi.proceed(this)嵌套调用一下拦截器，然后返回向上调用
 *              ========================================
 *              v 0 - ExposeInvocationInterceptor     ^---
 *              v 1 - AspectJAferThrowingAdvice       ^--- 执行「异常通知」
 *              v 2 - AfterReturningAdviceInterceptor ^---
 *              v 3 - AspectJAfterAdvice              ^--- finially中执行「」后置通知invokeAdviceMethod(getJoinPointMath(), null, null)
 *              v 4 - MethodBeforeAdviceInterceptor   ^--- 调用「」前置通知，再调用mi.proceed(this) --- 调用目标方法
 *              ========================================
 *
 *
 *   总结：
 *       1）@EnableAspectJAutoProxy 开启AOP功能
 *       2）@EnableAspectJAUtoProxy会给容器中注册一个组件 AnnotationAwareAspectJAutoProxyCreator
 *       3）AnnotationAwareAspectJAutoProxyCreator是一个后置处理器
 *       4）容器的创建流程
 *          1）regosterBeanPostProcessors()注册后置处理器，创建AnnotationAwareAspectJAutoProxyCreator
 *          2）finishBeanFactpruInitialization(beanFactory)；初始化剩下的单实例bean
 *             1）创建业务逻辑组件和切面组件
 *             2）AnnotationAwareAspectJAutoProxyCreator会拦截组件的创建过程
 *             3）组件创建完之后，判断组件是否需要增强
 *                是：切面的通知方法包装程增强器（Advisor）；给业务逻辑组件创建一个代理对象（Cglib）
 *       5）执行目标方法
 *          1）代理对象执行目标方法
 *          2）CglibAopProxy.intercept()
 *             1) 得到目标方法的拦截器链（增强器包装程拦截器MethodInterceptor）
 *             2）利用拦截器的链式机制，依次进入每一个拦截器进行执行
 *             3）效果：
 *                正常执行： 前置通知  -- 目标方法 -- 后置通知 -- 返回通知
 *                出现异常： 前置通知  -- 目标方法 -- 后置通知 -- 异常通知
 *
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
