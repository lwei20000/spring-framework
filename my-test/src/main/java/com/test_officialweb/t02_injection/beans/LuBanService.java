package com.test_officialweb.t02_injection.beans;

import org.springframework.stereotype.Component;

@Component
public class LuBanService {
	LuBanService(){
		System.out.println("luBan create ");
	}
}
