package com.test_shangguigu.t07_lifecircle.beans;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @Auther: weiliang
 * @Date: 2021/2/9 21:47
 * @Description:
 */
@Component
public class Dog {

	public Dog() {
		System.out.println("dog cunstructor...");
	}

	@PostConstruct
	public void init() {
		System.out.println("dog postConstruct...");
	}

	@PreDestroy
	public void destroy() {
		System.out.println("dog preDestroy...");
	}
}
