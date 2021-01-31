package com;

import org.springframework.beans.factory.ListableBeanFactory;

/**
 * @Auther: weiliang
 * @Date: 2021/1/31 16:15
 * @Description:
 */
public class ACUtils {


	public static void printAllBeans(ListableBeanFactory lbf) {
		String[] beanDefinitionNames = lbf.getBeanDefinitionNames();
		for(String beanDefinitionName : beanDefinitionNames){
			System.out.println(beanDefinitionName);
		}
	}
}
