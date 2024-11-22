package com.test20_ioc.ann;


import org.springframework.transaction.annotation.Transactional;

public interface PayService {

	void pay(String accountId, double money);
}
