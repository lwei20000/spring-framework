package com.ioc.xml;

/**
 * @Auther: weiliang
 * @Date: 2020/12/21 22:02
 * @Description:
 */
public class HelloService {
	private Student student;

	private Animal animal;

	public String hello() {
		return animal.getName();
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public Animal getAnimal() {
		return animal;
	}

	public void setAnimal(Animal animal) {
		this.animal = animal;
	}
}
