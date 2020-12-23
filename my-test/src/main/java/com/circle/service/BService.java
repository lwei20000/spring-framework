package com.circle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Auther: weiliang
 * @Date: 2020/12/23 15:27
 * @Description:
 */
@Component
public class BService {
	@Autowired
	private AService aService;
}
