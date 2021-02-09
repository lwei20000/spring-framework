package com.test_shangguigu.t07_lifecircle.config;


import com.test_shangguigu.t02_componentScan.beans.Persion;
import com.test_shangguigu.t06_import.ImportBeanDefinitionRegistrar.MyImportBeanDefinitionRegistrar;
import com.test_shangguigu.t06_import.factory.ColorFactoryBean;
import com.test_shangguigu.t06_import.selector.MyImportSelector;
import com.test_shangguigu.t07_lifecircle.beans.Car;
import com.test_shangguigu.t07_lifecircle.beans.Color;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.*;

/**
 * bean的生命周期
 *      bean的创建---初始化---销毁的过程
 * 容器管理bean的生命周期
 * 我们可以自定义初始化和销毁方法，容器在bean进行到当前生命周期的时候会调用我们的初始化和销毁方法。
 *
 * 构造对象（对象创建）
 *        单实例，在容器启动的时候创建对象
 *        多实例，在每次获取的时候创建对象
 * BeanPostProcessor.postProcessBeforeInitialization
 * 初始化：
 *       对象创建完成，并赋值好，调用初始化方法。。。
 * BeanPostProcessor.postProcessAfterInitialization
 * 销毁：
 *       单实例容器关闭的时候进行销毁
 *       多实例容器不会管理这个bean，容器不会调用销毁方法
 *
 *
 * 1）指定初始化和销毁分方法：
 *       通过@Bean指定init-method destroy-mothod
 * 2）通过Bean实现InitializingBean接口和
 *              DisposableBean接口
 * 3）可以使用JSR250规范定义的两个注解
 *              @PostConstruct 在bean创建完成并且属性复制完成
 *              @PreDestroy 在容器销毁bean之前通知我们进行清理工作
 * 4) BeanPostProcessor:bean的后置处理器，在bean初始化前进行一些处理工作。
 *              postProcessBeforeInitialization 在初始化之前工作
 *              postProcessAfterInitialization 在初始化之后工作
 *    工作原理：（调试技巧：断点打在postProcessBeforeInitialization中查看方法调用栈）
 *
 */
@ComponentScan({"com.test_shangguigu.t07_lifecircle.beans","com.test_shangguigu.t07_lifecircle.BeanPostProcessor"})
@Configuration
public class AppConfig07 {

	@Scope("prototype")
	@Bean(value = "car", initMethod = "init", destroyMethod = "destory")
	public Car car(){
		return new Car();
	}
}
