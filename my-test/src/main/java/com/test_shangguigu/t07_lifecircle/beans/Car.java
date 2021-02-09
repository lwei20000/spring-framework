package com.test_shangguigu.t07_lifecircle.beans;

/**
 * 假定这个Color是在别的包里面
 */
public class Car {
	public Car() {
		System.out.println("car constructor...");
	}

	public void init() {
		System.out.println("car init...");
	}

	public void destory() {
		System.out.println("car destory...");
	}
}
