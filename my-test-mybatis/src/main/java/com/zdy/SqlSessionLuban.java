package com.zdy;

import com.my.dao.CityMapper;

import java.lang.reflect.Proxy;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 21:19
 * @Description:
 */
public class SqlSessionLuban {

	public static Object getInstance() {
		// 实现接口
		// 返回对象
		// 能够查询
		Class<?>[] interfaces = new Class<?>[]{CityMapper.class};
		return Proxy.newProxyInstance(SqlSessionLuban.class.getClassLoader(), interfaces, new InvocationHandlerLuban());
	}












}
