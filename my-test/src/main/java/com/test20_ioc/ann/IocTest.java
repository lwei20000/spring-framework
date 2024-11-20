package com.test20_ioc.ann;

import com.test20_ioc.ann.config.AppConfig;
import com.beans.PayService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * https://blog.csdn.net/qq_41907991/article/details/105667900
 * https://blog.csdn.net/qq_41907991/article/details/105743462
 *
 * 测试：AnnotationConfigApplicationContext
 * 测试：使用配置类AppConfig
 * 注意：
 *   默认构造方法：
 *   【1】AnnotatedBeanDefinitionReader --- 注册单个的BeanDefinition
 *   这里面注册了一堆后置处理器
 *   ---ConfigurationClassPostProcessor --- 后置处理器发挥作用（）
 *   ---AutowiredAnnotationBeanPostProcessor --- 后置处理器
 *   【2】ClassPathBeanDefinitionScanner --- 注册所有扫描到的BeanDefinition
 *   ---
 *   ---
 *
 *   其它相干类：
 *   ClassPathBeanDefinitionScanner --- 类路径下的BeanDefinition的扫描器
 *
 */
public class IocTest {

	public static void main(String[] args) {
		// 测试annotation方式取得bean
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
		PayService user = (PayService)ac.getBean("payService");
		System.out.println(user);

		// 查看annotation方式生成的ioc容器中，默认添加的bean
		// org.springframework.context.annotation.internalConfigurationAnnotationProcessor———ConfigurationClassPostProcessor
		// org.springframework.context.annotation.internalAutowiredAnnotationProcessor———————AutowiredAnnotationBeanPostProcessor
		// org.springframework.context.annotation.internalCommonAnnotationProcessor————————--CommonAnnotationBeanPostProcessor
		// org.springframework.context.event.internalEventListenerProcessor
		// org.springframework.context.event.internalEventListenerFactory
		String[] beanDefinitionNames = ac.getBeanDefinitionNames();
		for(String beanDefinitionName : beanDefinitionNames){
			System.out.println(beanDefinitionName);
		}
	}
}