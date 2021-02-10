package com.test_shangguigu.t21_aop;


import com.ACUtils;
import com.test_shangguigu.t21_aop.config.AppConfig21;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class IocTest {
	public static void main(String[] args) {

		// 创建ioc容器
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig21.class);
		ACUtils.printAllBeans(ac); // 打印所有bean

		// 不要这样自己创建对象，因为这样自己new对象，不能得到增强的bean
		//MathCalculator mathCalculator = new MathCalculator();
		//mathCalculator.div(4,2);

		MathCalculator mathCalculator = ac.getBean(MathCalculator.class);
		mathCalculator.div(4,1);



		ac.close();
	}
}