package com.test_officialweb.t08_BeanPostProcessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @Auther: weiliang
 * @Date: 2021/1/27 10:49
 * @Description:
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (beanName.equals("indexService")) {
			System.out.println(bean);
			System.out.println("bean config invoke postProcessBeforeInitialization");
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (beanName.equals("indexService")) {
			System.out.println(bean);
			System.out.println("bean config invoke postProcessAfterInitialization");
		}
		return bean;
	}
}
