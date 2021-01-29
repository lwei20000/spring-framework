package com.test_officialweb.t08_BeanPostProcessor.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Auther: weiliang
 * @Date: 2021/1/27 10:50
 * @Description:
 */
@Component
public class IndexService {
	@Autowired
	LuBanService luBanService;

	@Override
	public String toString() {
		return "IndexService{" +
				"luBanService=" + luBanService +
				'}';
	}
}
