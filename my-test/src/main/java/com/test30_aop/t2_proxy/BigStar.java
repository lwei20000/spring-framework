package com.test30_aop.t2_proxy;

/**
 * @Auther: weiliang
 * @Date: 2024/11/6 10:00
 * @Description:
 */
public class BigStar implements Star{

	private String name;

	public String sing() {
		System.out.println("唱歌。。。");
		return "xiexie";

	}

	public void dance() {
		System.out.println("跳舞。。。");
	}
}
