package com.test_officialweb.t02_injection.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Service {
	private LuBanService luBanService;

	public Service() {
		System.out.println("service create");
	}

	public void test(){
		System.out.println(luBanService);
	}

	// 通过autowired指定使用set方法完成注入
	@Autowired
	public void setLuBanService(LuBanService luBanService) {
		System.out.println("注入luBanService by setter");
		this.luBanService = luBanService;
	}
}
