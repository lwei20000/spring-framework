package com.test_officialweb.t07_FactoryBean;

import com.test_officialweb.t07_FactoryBean.beans.TestBean;
import org.springframework.beans.factory.FactoryBean;

/**
 * @Auther: weiliang
 * @Date: 2021/1/27 09:26
 * @Description:
 */
public class MyFactoryBean implements FactoryBean<Object> {
	@Override
	public Object getObject() throws Exception {
		System.out.println("执行了一段复杂的创建Bean的逻辑");
		return new TestBean();
	}

	@Override
	public Class<?> getObjectType() {
		return TestBean.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}