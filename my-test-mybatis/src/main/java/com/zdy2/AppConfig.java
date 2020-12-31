package com.zdy2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 20:17
 * @Description:
 */
@ComponentScan("com.zdy2")
@Configuration
@MapperScanLuban   // MapperScanLuban------ImportBeanDefinitionRegistrar2（循环包中所有的mapper）
public class AppConfig {
}
