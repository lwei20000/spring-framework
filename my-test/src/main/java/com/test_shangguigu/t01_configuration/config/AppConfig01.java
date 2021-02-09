package com.test_shangguigu.t01_configuration.config;


import com.test_shangguigu.t01_configuration.beans.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig01 {

	// 给容器注册一个bean，类型为返回值，id默认为方法名
	@Bean()
	public Person persion(){
		return new Person("lisan", 20);
	}
}
