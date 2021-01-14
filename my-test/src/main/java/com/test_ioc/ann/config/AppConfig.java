package com.test_ioc.ann.config;


import com.test_ioc.ann.service.PayService;
import com.test_ioc.ann.service.PayServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean("payService")
	public PayService getPayService(){
		return new PayServiceImpl();
	}
}
