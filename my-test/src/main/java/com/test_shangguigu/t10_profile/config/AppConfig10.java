package com.test_shangguigu.t10_profile.config;

import com.test_shangguigu.t06_import.beans.Yellow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.*;
import org.springframework.util.StringValueResolver;

/**
 * Profile:
 *     Spring为我们提供的可以根据当前环境，动态的激活和切换一系列的功能：
 *
 * 开发环境；测试环境；生产环境：
 * 数据源（/A）（/B）（/C）
 *
 * 注解@Profile：指定组件在那个环境的情况下才被注册到容器中，不指定，任何环境下都能注册这个组件
 * 1) 加了环境标示的bean，只有这个环境被激活的时候才能注册到容器，不激活环境不会被注册到容器中。默认是default环境
 * 2）写在配置类上，只有在指定的环境的时候，整个类里面的所有配置才能生效。
 * 3）没有标注环境标示的bean在任何环境下都是加载的
 *
 * 【切换环境的方法】
 *           1) 使用命令行参数切换到test环境：VM arguments：-Dspring.profiles=test
 *           2）硬编码（参见test示例）
 *
 *
 *
 *
 *
 *
 */
@Configuration
@PropertySource("classpath:/com/test_shangguigu/dbconfig.properties")
public class AppConfig10 implements EmbeddedValueResolverAware {

	@Value("${db.user}")
	private String user;

	private String driverClass;

	// 值解析器
	private StringValueResolver stringValueResolver;

	@Profile("test")
	@Bean
	public Yellow yellow() {
		return new Yellow();
	}

	@Profile("dev")
	@Bean("devDataSource")
	public Object dataSourceDev(@Value("${db.password}") String pwd) {
		driverClass = stringValueResolver.resolveStringValue("${db.driverClass}");
		return new Object();
	}

	@Profile("test")
	@Bean("testDataSource")
	public Object dataSourceTest(@Value("${db.password}") String pwd) {
		return new Object();
	}

	@Profile("prod")
	@Bean("prodDataSource")
	public Object dataSourceProd(@Value("${db.password}") String pwd) {
		return new Object();
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		// 拿到值解析器，使用值解析器来解析表达式
		this.stringValueResolver = resolver;
	}
}
