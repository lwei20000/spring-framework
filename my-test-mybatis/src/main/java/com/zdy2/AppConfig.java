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
@MapperScanLuban
public class AppConfig {
}
