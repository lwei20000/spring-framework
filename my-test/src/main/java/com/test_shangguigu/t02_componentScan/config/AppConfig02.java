package com.test_shangguigu.t02_componentScan.config;


import com.test_shangguigu.t02_componentScan.beans.Persion;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Configuration
//@ComponentScan(value="com.test_shangguigu.t02_componentScan",includeFilters = {
//		@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Controller.class})
//},useDefaultFilters = false)

@ComponentScans(
		value = {
				@ComponentScan(value="com.test_shangguigu.t02_componentScan"
				                ,includeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Controller.class})}
                                ,useDefaultFilters = false)
		}
)
public class AppConfig02 {

	// 给容器注册一个bean，类型为返回值，id默认为方法名
	@Bean()
	public Persion persion(){
		return new Persion("lisan", 20);
	}
}
