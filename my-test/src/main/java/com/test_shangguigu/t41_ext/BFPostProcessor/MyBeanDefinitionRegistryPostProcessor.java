package com.test_shangguigu.t41_ext.BFPostProcessor;

import com.test_shangguigu.t06_import.beans.Blue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.stereotype.Component;

@Component
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	// BeanDefinitionRegistry : Bean定语你信息的保存中心。
	// 以后BeanFactory就是按照BeandefinitionRegistry里面保存的每一个bean定义信息创建bean实例的。
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

		System.out.println("postProcessBeanDefinitionRegistry...bean的数量" + registry.getBeanDefinitionCount());
		//RootBeanDefinition rbd = new RootBeanDefinition(Blue.class);
		AbstractBeanDefinition abd = BeanDefinitionBuilder.rootBeanDefinition(Blue.class).getBeanDefinition();
		registry.registerBeanDefinition("hello", abd);
	}

	//
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("MyBeanDefinitionRegistryPostProcessor...bean的数量" + beanFactory.getBeanDefinitionCount());
	}
}
