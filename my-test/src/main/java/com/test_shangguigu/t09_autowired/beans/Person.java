package com.test_shangguigu.t09_autowired.beans;

import org.springframework.beans.factory.annotation.Value;

public class Person {

	/**
	 * 使用@Value赋值
	 *   1、基本数值
	 *   2、可以些SpEl， #{}
	 *   3、可以些${},取出配置文件【properties】中的值（在运行环境变量里面的值）
	 *
	 */
	@Value("zhangsan")
	private String name;

	private int age;

	@Value("${person.nickName}")
	public String nickName;

	public Person(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@Override
	public String toString() {
		return name.toString() +" "+ age  +" "+  nickName.toString();
	}
}
