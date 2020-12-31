package com.zdy2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 20:59
 * @Description:
 */
@Component
public class CityService {

	@Autowired
	CityMapper cityMapper;

	public void queryAll() {
		cityMapper.query();
	}
}
