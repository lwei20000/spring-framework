package com.zdy2;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * 在spring中，FactoryBean负责产生
 *
 * 普通bean是可以通过API直接获取
 * 工厂bean具备普通bean的一切特点，但是他通过getBean获取到的不是本身，而是工厂生产的bean。
 *
 *
 * FactoryBeanLuban只能生成一种mapper。
 * 我们希望FactoryBeanLuban1能够帮我们产生不同的mapper，
 * (1)xml
 * 那么使用了xml配置文件，然后AppConfig中添加@ImportResource就实现了定制化配置生成不同的mapper
 * (2)注解@Component
 * 但是这样有一个矛盾，一旦我们使用@Component来注解factory，这个类必然是spring自己来实例化，而
 * spring自己实例化的话我们没有办法去干预它的实例化过程。
 * 于是问题编程：spring零配置实例话一个bean的时候如何指定他的构造方法参数？
 * 如何解决====>postProcessor后置处理器
 * 动态往spring中注册一个类====>ImportBeanDefinitionRegistrar
 *
 *
 */
public class FactoryBeanLuban2 implements FactoryBean<Object> {

	Object mapperInterface1;

	public FactoryBeanLuban2(Class<?> mapperInterface) {
		this.mapperInterface1 = mapperInterface;
	}

	@Override
	public Object getObject() throws Exception {

		Class<?>[] interfaces = new Class<?>[]{CityMapper.class};
		return Proxy.newProxyInstance(SqlSessionLuban.class.getClassLoader(), interfaces, new InvocationHandlerLuban());
	}

	@Override
	public Class<?> getObjectType() {
		return CityMapper.class;
	}

}
