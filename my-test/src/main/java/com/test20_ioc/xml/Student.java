package com.test20_ioc.xml;

import java.util.List;

/**
 * @Auther: weiliang
 * @Date: 2020/12/21 21:37
 * @Description:
 */
public class Student {

	private String name;
	private Integer age;
	private List<String> classList;

	public Student() {
		this.name = name;
		this.age = age;
	}

	public Student(String name, Integer age) {
		this.name = name;
		this.age = age;
	}


	public List<String> getClassList() {
		return classList;
	}

	public void setClassList(List<String> classList) {
		this.classList = classList;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}




}
