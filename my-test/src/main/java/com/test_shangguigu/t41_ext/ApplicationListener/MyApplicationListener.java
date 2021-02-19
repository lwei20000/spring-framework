package com.test_shangguigu.t41_ext.ApplicationListener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @Auther: weiliang
 * @Date: 2021/2/19 22:13
 * @Description:
 */
@Component
public class MyApplicationListener implements ApplicationListener<ApplicationEvent> {

	// 当容器中发布此事件以后，方法触发
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		System.out.println("收到事件" + event);
	}
}
