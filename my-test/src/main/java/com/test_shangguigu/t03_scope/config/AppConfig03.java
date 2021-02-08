package com.test_shangguigu.t03_scope.config;


import com.test_shangguigu.t02_componentScan.beans.Persion;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Controller;


@Configuration
public class AppConfig03 {

	/**
	 * prototype 如果是原型模式的情况下，在容器创建的时候并不会生成bean实例
	 * singleton 默认，单例模式下的bean，在容器创建的时候就会生成bean实例
	 * request
	 * session
	 */
	@Scope("prototype")
	@Bean("person")
	public Persion persion(){
		System.out.println("给容器中添加bean。。。");
		return new Persion("zhangsan", 20);
	}
}
