package com.test12_circle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("bService")
public class BService {
	@Autowired
	//@Resource
	private AService aService;

	public void testB() {
		aService.testA();
	}
}
