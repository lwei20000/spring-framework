package com.my.dao;


import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 20:19
 * @Description:
 */
public interface CityMapper {

	@Select({ "select * from city " })
	public List<Map<String,Object>> query();
}
