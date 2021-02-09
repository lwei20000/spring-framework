package com.test_shangguigu.t07_lifecircle.config;


import com.test_shangguigu.t02_componentScan.beans.Persion;
import com.test_shangguigu.t06_import.ImportBeanDefinitionRegistrar.MyImportBeanDefinitionRegistrar;
import com.test_shangguigu.t06_import.factory.ColorFactoryBean;
import com.test_shangguigu.t06_import.selector.MyImportSelector;
import com.test_shangguigu.t07_lifecircle.beans.Car;
import com.test_shangguigu.t07_lifecircle.beans.Color;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * bean的生命周期
 *      bean的创建---初始化---销毁的过程
 * 容器管理bean的生命周期
 * 我们可以自定义初始化和销毁方法，容器在bean进行到当前生命周期的时候会调用我们的初始化和销毁方法。
 *    1）指定初始化和销毁分方法：
 *       init-method destroy-mothod
 */
@Configuration
public class AppConfig07 {

	/**
	 *
	 */



	@Bean(value = "car", initMethod = "init", destroyMethod = "destory")
	public Car car(){
		return new Car();
	}
}
