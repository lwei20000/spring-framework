package com.test_shangguigu.t06_import.config;


import com.test_shangguigu.t02_componentScan.beans.Persion;
import com.test_shangguigu.t05_conditional.condition.LinuxConditon;
import com.test_shangguigu.t05_conditional.condition.MacConditon;
import com.test_shangguigu.t05_conditional.condition.WindowsConditon;
import com.test_shangguigu.t06_import.ImportBeanDefinitionRegistrar.MyImportBeanDefinitionRegistrar;
import com.test_shangguigu.t06_import.beans.Color;
import com.test_shangguigu.t06_import.factory.ColorFactoryBean;
import com.test_shangguigu.t06_import.selector.MyImportSelector;
import org.springframework.context.annotation.*;


@Configuration
@Import({Color.class, MyImportSelector.class, MyImportBeanDefinitionRegistrar.class})  // Color模拟假设在其它的包中，这里import之后，容器中就有Color对应的bean了。
public class AppConfig06 {

	/**
	 * 给容器中注入组件的方式：
	 * 1、包扫描+组件标注注解（@Contruller @Service @Repository @Component）
	 * 2、@Bean【导入第三方包里面的组件】
	 * 3、@Import【快速给容器中导入一个组件】
	 *    1）@Import（要导入到容器中的组件）
	 *    2）ImportSelector；返回要导入到容器中的组件的全类名数组
	 *    3）ImportBeanDefinitionRegistrar：手动注册bean到容器中
	 * 4、使用Spring提供的FactoryBean（工厂bean）
	 *    1) 默认获取到的是工厂bean调用getObject创建的对象
	 *    2) 要获取工厂bean本身，我们需要给id加一个前缀&
	 *
	 *
	 */


	/**
	 */
	@Bean("person")
	public Persion persion(){
		System.out.println("给容器中添加bean。。。");
		return new Persion("zhangsan", 20);
	}

	@Bean
	public ColorFactoryBean colorFactoryBean() {
		return new ColorFactoryBean();
	}


}
