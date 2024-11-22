package com.test20_ioc.circle.test02_NG;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("bService")
public class BService {
	@Autowired
	private AService aService;

	public void testB() {
		aService.testA();
	}
}
