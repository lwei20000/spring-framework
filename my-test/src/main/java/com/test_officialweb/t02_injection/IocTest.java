package com.test_officialweb.t02_injection;

import com.ACUtils;
import com.beans.Bird;
import com.test_officialweb.t02_injection.beans.Service;
import com.test_officialweb.t02_injection.config.AppConfig02;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 */
public class IocTest {

	public static void main(String[] args) {

		// config类主要完成对类的扫描
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig02.class);
		Service service = (Service) ac.getBean("service");
		service.test();

		// 查看xml方式生成的ioc容器中，默认添加的bean
		// 对比annotation的方式，xml方式只有文件中配置的bean。没有多余的后置处理器。
		ACUtils.printAllBeans(ac);
	}
}