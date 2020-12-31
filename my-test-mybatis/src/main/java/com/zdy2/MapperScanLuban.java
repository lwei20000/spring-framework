package com.zdy2;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 23:31
 * @Description:
 */
@Import(ImportBeanDefinitionRegistrar2.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapperScanLuban {
}
