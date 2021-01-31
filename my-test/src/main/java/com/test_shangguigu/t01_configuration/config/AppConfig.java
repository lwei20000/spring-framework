package com.test_shangguigu.t01_configuration.config;


import com.beans.PayService;
import com.beans.PayServiceImpl;
import com.test_shangguigu.t01_configuration.beans.Persion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	// 给容器注册一个bean，类型为返回值，id默认为方法名
	@Bean()
	public Persion persion(){
		return new Persion("lisan", 20);
	}
}
