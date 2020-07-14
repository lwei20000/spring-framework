package com.kittycoder.cycleIndependence.config;


import com.kittycoder.cycleIndependence.service.PayService;
import com.kittycoder.cycleIndependence.service.PayServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean("payService")
	public PayService getPayService(){
		return new PayServiceImpl();
	}
}
