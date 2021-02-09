package com.test_shangguigu.t06_import.selector;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Auther: weiliang
 * @Date: 2021/2/8 15:40
 * @Description:
 */
public class MyImportSelector implements ImportSelector {
	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {


		// 可以返回空数组，不能返回null
		return new String[]{"com.test_shangguigu.t06_import.beans.Red","com.test_shangguigu.t06_import.beans.Blue"};
	}
}
