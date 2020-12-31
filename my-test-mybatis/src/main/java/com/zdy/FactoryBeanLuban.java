package com.zdy;

import com.my.dao.CityMapper;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * 在spring中，FactoryBean负责产生
 *
 * 普通bean是可以通过API直接获取
 * 工厂bean具备普通bean的一切特点，但是他通过getBean获取到的不是本身，而是工厂生产的bean。
 *
 */
public class FactoryBeanLuban implements FactoryBean<Object> {

	@Override
	public Object getObject() throws Exception {
		CityMapper mapper =  (CityMapper)SqlSessionLuban.getInstance();
		return mapper;
	}

	@Override
	public Class<?> getObjectType() {
		return CityMapper.class;
	}
}
