package com.test_shangguigu.t06_import.ImportBeanDefinitionRegistrar;

import com.test_shangguigu.t06_import.beans.Rainbow;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Auther: weiliang
 * @Date: 2021/2/9 13:43
 * @Description:
 */
public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		boolean red = registry.containsBeanDefinition("appConfig06");  // 判断容器中是否有某一个bean
		if(red) {
			BeanDefinition bd = new RootBeanDefinition(Rainbow.class);
			registry.registerBeanDefinition("rainbow", bd);
		}
	}

}
