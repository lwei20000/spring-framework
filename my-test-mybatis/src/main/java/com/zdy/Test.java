package com.zdy;

import com.my.app.AppConfig;
import com.my.dao.CityMapper;
import com.my.service.CityService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * 我们自己通过动态代理，模拟mybatis的过程
 * SqlSessionLuban中通过Proxy.newInstance生成动态代理类。
 */
public class Test {
	public static void main(String[] args) {
		CityMapper mapper =  (CityMapper)SqlSessionLuban.getInstance();
		mapper.query();
	}
}
