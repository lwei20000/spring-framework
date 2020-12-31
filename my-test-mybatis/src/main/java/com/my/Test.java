package com.my;

import com.my.app.AppConfig;
import com.my.service.CityService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 20:58
 * @Description:
 */
public class Test {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
		ac.getBean(CityService.class).queryAll();
	}
}
