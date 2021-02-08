package com.test_shangguigu.t04_lazyBean.config;


import com.test_shangguigu.t02_componentScan.beans.Persion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;


@Configuration
public class AppConfig04 {

	/**
	 * prototype 如果是原型模式的情况下，在容器创建的时候并不会生成bean实例
	 * singleton 默认，单例模式下的bean，在容器创建的时候就会生成bean实例
	 * request
	 * session
	 */
	@Scope("singleton")
	@Bean("person")
	/**
	 * scope在默认的singleton的情况家，容器创建的时候会生成bean，
	 * 加上这个@Lazy注解后就会延迟到获取的时候蔡创建bean
	 */
	@Lazy
	public Persion persion(){
		System.out.println("给容器中添加bean。。。");
		return new Persion("zhangsan", 20);
	}
}
