package com.zdy2;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 23:22
 * @Description:
 */
public class ImportBeanDefinitionRegistrar2 implements ImportBeanDefinitionRegistrar {

	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
										BeanDefinitionRegistry registry,
										BeanNameGenerator importBeanNameGenerator) {

		// 此处可以循环某个包下所有的mapper
		// for (int i = 0; i < ; i++) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FactoryBeanLuban2.class);
		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		// 此处还可以通过beanDefinition获得参数
		beanDefinition.getConstructorArgumentValues().addGenericArgumentValue("com.zdy2.CityMapper");
		registry.registerBeanDefinition("xxx", beanDefinition);
	}
}
























