package com.test_shangguigu.t05_conditional.condition;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @Auther: weiliang
 * @Date: 2021/2/8 12:37
 * @Description:
 */
public class LinuxConditon implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		// 获取当前环境信息
		Environment environment = context.getEnvironment();
		String osName = environment.getProperty("os.name");
		if(osName.contains("linux")) {
			return true;
		} else {
			return false;
		}
	}
}
