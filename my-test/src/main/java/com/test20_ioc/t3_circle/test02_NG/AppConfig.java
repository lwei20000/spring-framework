package com.test20_ioc.t3_circle.test02_NG;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync  // 开启异步
@ComponentScan("com.test20_ioc.t3_circle.test02_NG")
public class AppConfig {
}
