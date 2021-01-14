package com.beans;

/**
 * @Auther: weiliang
 * @Date: 2020/12/21 22:18
 * @Description:
 */
public class AnimalFactory {
	public static Animal getAnimal(String type) {
		if("dog".equals(type)) {
			return new Dog();
		} else {
			return new Cat();
		}
	}
}
