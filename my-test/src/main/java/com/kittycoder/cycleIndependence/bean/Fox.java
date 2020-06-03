package com.kittycoder.cycleIndependence.bean;

import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class Fox {

	public Fox(User user) {
		System.out.println("调用Fox()构造器");
	}
}