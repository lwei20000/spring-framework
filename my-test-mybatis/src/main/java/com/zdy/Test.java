package com.zdy;

import com.my.dao.CityMapper;

/**
 * 我们自己通过动态代理，模拟mybatis的过程
 * SqlSessionLuban中通过Proxy.newInstance生成动态代理类。
 */
public class Test {
	public static void main(String[] args) {
		CityMapper mapper =  (CityMapper)SqlSessionLuban.getInstance();
		mapper.query();
	}
}
