package com.test_shangguigu.t41_ext.BFPostProcessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("MyBeanFactoryPostProcessor...postProcessor");
		int count = beanFactory.getBeanDefinitionCount();
		String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
		System.out.println("当前beanFactory中有 " + count +"个bean定义");
		System.out.println(Arrays.asList(beanDefinitionNames));
	}
}
