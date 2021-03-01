package com.test_officialweb.t08_BeanPostProcessor.config;

import com.test_officialweb.t08_BeanPostProcessor.beans.IndexService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean("indexService")
	public IndexService getIndexService(){
		return new IndexService();
	}
}
