package com.zdy1;

import com.my.dao.CityMapper;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * 在spring中，FactoryBean负责产生
 *
 * 普通bean是可以通过API直接获取
 * 工厂bean具备普通bean的一切特点，但是他通过getBean获取到的不是本身，而是工厂生产的bean。
 *
 * FactoryBeanLuban只能生成一种mapper。
 * 我们希望FactoryBeanLuban1能够帮我们产生不同的mapper，
 * 那么使用了xml配置文件，然后AppConfig中添加@ImportResource就实现了定制化配置生成不同的mapper
 *
 */
public class FactoryBeanLuban1 implements FactoryBean<Object> {

	Object mapperInterface1;

	@Override
	public Object getObject() throws Exception {

		Class<?>[] interfaces = new Class<?>[]{CityMapper.class};
		return Proxy.newProxyInstance(SqlSessionLuban.class.getClassLoader(), interfaces, new InvocationHandlerLuban());
	}

	@Override
	public Class<?> getObjectType() {
		return CityMapper.class;
	}

	public Object getMapperInterface1() {
		return mapperInterface1;
	}

	public void setMapperInterface1(Object mapperInterface1) {
		this.mapperInterface1 = mapperInterface1;
	}
}
