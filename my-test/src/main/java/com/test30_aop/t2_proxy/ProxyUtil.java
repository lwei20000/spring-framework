package com.test30_aop.t2_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Auther: weiliang
 * @Date: 2024/11/6 10:05
 * @Description:
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ProxyUtil {
	public static Star createProxy(BigStar bigStar) {
		/* newProxyInstance(ClassLoader Loader,
		Closs<?>[] interfaces,
		InvocotionHandler h)
		参数1: 用于指定一个类加载器
		参数2:指定生成的代理长什么样子，也就是有哪些方汉
		零数3:用来指定生成前代理对象要于什么事情
		// Star storProxy = ProxyUtil.createProxy(s);
		// starProxy.sing("好日 starProxy.donce()
		 */
		Star starProxy = (Star) Proxy.newProxyInstance(
				ProxyUtil.class.getClassLoader(),
				new Class[]{Star.class},
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// 代理对象要做的事情，会在这里写代码
						if (method.getName().equals("sing")) {
							System.out.println("准备话筒，收钱20万");
							return method.invoke(bigStar, args);
						} else if (method.getName().equals("dance")) {
							System.out.println("准备场地，收钱1000万");
							return method.invoke(bigStar, args);
						} else {
							return method.invoke(bigStar, args);
						}
					}
				});
		return starProxy;
	}
}