package com.test12_circle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @Auther: weiliang
 * @Date: 2020/12/23 15:27
 * @Description:
 */
@Component("aService")
public class AService {
	@Autowired
	//@Resource
	private BService bService;

	@Async
	public void testA() {

	}
}
