package com.zdy1;

import com.my.dao.CityMapper;
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
