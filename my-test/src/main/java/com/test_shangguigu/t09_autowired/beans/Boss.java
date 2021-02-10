package com.test_shangguigu.t09_autowired.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 默认加载ioc容器的组件，容器启动会调用无参构造器创建对象，再进行初始化赋值等操作
@Component
public class Boss {

	//@Autowired
	public Boss(@Autowired Car car) { // 只有一个有参构造器的场景下，这个参数上的@Autowired是可以省略的
		System.out.println("Boss 有参构造器。。。");
		this.car = car;
	}

	//@Autowired
	private Car car;

	public Car getCar() {
		return car;
	}

	//@Autowired
	// 标注在方法，Spring容器创建当前对象，就会调用方法，完成赋值
	// 方法使用的参数，自定义类型的值从ioc容器中获取
	public void setCar(Car car) {
		this.car = car;
	}
}
