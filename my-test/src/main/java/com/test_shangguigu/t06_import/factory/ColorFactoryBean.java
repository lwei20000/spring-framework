package com.test_shangguigu.t06_import.factory;

import com.test_shangguigu.t06_import.beans.Color;
import org.springframework.beans.factory.FactoryBean;

/**
 * @Auther: weiliang
 * @Date: 2021/2/9 13:58
 * @Description:
 */
public class ColorFactoryBean implements FactoryBean<Color> {

	// 返回一个Color对象，这个对象会添加到容器中
	@Override
	public Color getObject() throws Exception {
		return new Color();
	}

	@Override
	public Class<?> getObjectType() {
		return Color.class;
	}

	// 是单例？
	// true：这个bean是单利，在容器中保存一份
	// false：多例，每次获取都会创建一个新的bean
	@Override
	public boolean isSingleton() {
		return true;
	}
}
