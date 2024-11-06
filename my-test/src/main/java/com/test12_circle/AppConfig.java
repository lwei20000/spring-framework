package com.test12_circle;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync  // 开启异步
@ComponentScan("com.test12_circle")
public class AppConfig {
}
