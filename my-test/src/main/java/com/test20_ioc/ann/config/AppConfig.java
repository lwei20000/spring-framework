package com.test20_ioc.ann.config;


import com.beans.PayService;
import com.beans.PayServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean("payService")
	public PayService getPayService(){
		return new PayServiceImpl();
	}
}
