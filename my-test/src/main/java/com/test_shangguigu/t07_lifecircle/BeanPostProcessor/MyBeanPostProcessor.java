package com.test_shangguigu.t07_lifecircle.BeanPostProcessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @Auther: weiliang
 * @Date: 2021/2/9 21:53
 * @Description:
 */
@Component  // 将后置处理器加入容器中
public class MyBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("postProcessBeforeInitialization。。。" + beanName + "=>" + bean);
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("postProcessAfterInitialization。。。" + beanName + "=>" + bean);
		return bean;
	}
}
