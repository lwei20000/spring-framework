package com.test21_aop.aoptest;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @Auther: weiliang
 * @Date: 2020/12/14 22:36
 * @Description:
 */
public class MethodCounter {

	private HashMap<String, Integer> map = new HashMap<String, Integer>();
	private int allcount;

	protected void count(Method method) {
		count(method.getName());
	}
	protected void count(String methodName) {
		Integer i = map.get(methodName);
		i = (i != null) ? new Integer(i.intValue() + 1) : new Integer(i);
		map.put(methodName, i);
		++ allcount;
	}
	public int getCalls(String methodName) {
		Integer i = map.get(methodName);
		return (i != null ? i.intValue():0);
	}
	public int getCalls() {
		return allcount;
	}
	public boolean equals(Object other) {
		return (other != null && other.getClass() == this.getClass());
	}
	public int hashCode() {
		return getClass().hashCode();
	}

}
