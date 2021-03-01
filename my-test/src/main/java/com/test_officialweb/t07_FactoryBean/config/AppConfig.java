package com.test_officialweb.t07_FactoryBean.config;

import com.test_officialweb.t07_FactoryBean.factoryBean.MyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean("myFactoryBean")
	public MyFactoryBean getMyFactoryBean(){
		return new MyFactoryBean();
	}
}
