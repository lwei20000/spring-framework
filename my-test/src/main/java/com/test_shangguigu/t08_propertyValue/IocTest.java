package com.test_shangguigu.t08_propertyValue;


import com.ACUtils;
import com.test_shangguigu.t08_propertyValue.beans.Person;
import com.test_shangguigu.t08_propertyValue.config.AppConfig08;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class IocTest {
	public static void main(String[] args) {

		// 创建ioc容器
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig08.class);
		ACUtils.printAllBeans(ac); // 打印所有bean

		Person bean = (Person) ac.getBean("persion");
		System.out.println(bean);

		ac.close();
	}
}