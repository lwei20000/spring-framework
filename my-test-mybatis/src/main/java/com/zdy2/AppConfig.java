package com.zdy2;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 20:17
 * @Description:
 */
@ComponentScan("com.zdy1")
@Configuration
@ImportResource("classpath:com/zdy1/spring.xml")
public class AppConfig {
}
