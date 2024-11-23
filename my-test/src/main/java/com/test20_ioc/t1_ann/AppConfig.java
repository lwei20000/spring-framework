package com.test20_ioc.t1_ann;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean("payService")
	public PayService getPayService(){
		return new PayServiceImpl();
	}
}