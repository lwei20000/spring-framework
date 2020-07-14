package com.kittycoder.cycleIndependence.service;


import org.springframework.transaction.annotation.Transactional;

public interface PayService {

	@Transactional
	void pay(String accountId, double money);
}
