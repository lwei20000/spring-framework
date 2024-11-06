package com.test01_java.jvm;

public class test_captal8_6 {
	static abstract class Human{}
	static class Man extends Human {}
	static class Woman extends Human {}

	public void sayHello(Human human) {
		System.out.println("Hello Human");
	}
	public void sayHello(Man man) {
		System.out.println("Hello man");
	}
	public void sayHello(Woman Woman) {
		System.out.println("Hello woman");
	}

	public static void main(String[] args) {
		Human man = new Man();
		Human woman = new Woman();
		test_captal8_6 test = new test_captal8_6();
		test.sayHello(man);
		test.sayHello(woman);
	}
}
