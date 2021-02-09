package com.test_shangguigu.t08_propertyValue.config;

import com.test_shangguigu.t08_propertyValue.beans.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * 指定配置文件路径的方法：
 * 		xml方式使用配置文件： <context:property-placeHolder location="classpath:person.properties"/>
 * 		注解方式使用配置文件：参照本例@PropertySource
 *
 * 取得配置文件中属性的方法：
 *      bean中的@Value注解
 *      applicationContext.getEnviroment(); enviroment.getProperty("person.nickName");
 *
 */
@PropertySource(value="classpath:com/test_shangguigu/person.properties")
@Configuration
public class AppConfig08 {

	@Bean("persion")
	public Person persion(){
		return new Person("",0);
	}
}
