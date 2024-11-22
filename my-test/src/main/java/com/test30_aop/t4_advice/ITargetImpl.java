package com.test30_aop.t4_advice;

/**
 * 目标的实现
 * @Auther: weiliang
 * @Date: 2020/12/15 10:04
 * @Description:
 */
public class ITargetImpl implements ITarget {

	@Override
	public void say() {
		System.out.println("==================>say");
	}
}
