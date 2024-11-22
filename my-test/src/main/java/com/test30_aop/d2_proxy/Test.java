package com.test30_aop.d2_proxy;

/**
 * @Auther: weiliang
 * @Date: 2024/11/6 10:21
 * @Description:
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Test {
	public static void main(String[] args) {
		BigStar star = new BigStar();
		Star starProxy = ProxyUtil.createProxy(star);

		String s = starProxy.sing();
		starProxy.dance();
	}
}
