package com.proxy;

import java.lang.reflect.Proxy;

/**
 * @Auther: weiliang
 * @Date: 2020/12/25 14:54
 * @Description:
 */
public class Test {

	public static void main(String[] args) {

		//实例化目标对象（创造一个力宏）
		WangLiHong liHong = new WangLiHong();

		//实例化调用处理类（编好的故事）
		MyStoryInvocationHandler handler = new MyStoryInvocationHandler(liHong);

		//创建代理
		ClassLoader loader =   Test.class.getClassLoader();
		Class<?>[] interfaces = liHong.getClass().getInterfaces();
		Singer proxy = (Singer) Proxy.newProxyInstance(loader, interfaces, handler);

		//调用点歌
		proxy.orderSong("就是现在");

		//调用-再见
		proxy.sayGoodBye("zhangchengzi");

	}
}
