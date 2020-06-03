package com.kittycoder.cycleIndependence.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class User {

	@Autowired
	private Fox fox;

	public User(Fox fox) {
		System.out.println(fox);
		System.out.println("调用User()构造器");;
	}
}
