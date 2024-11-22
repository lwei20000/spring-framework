package com.test01_java.threadlocal02;

/**
 * @Auther: weiliang
 * @Date: 2024/11/5 10:38
 * @Description:
 */
public class ThreadLocalExample {
	private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

	public static void main(String[] args) {
		new Thread(() -> {
			threadLocal.set("Thread A's data");
			print();
			threadLocal.remove();
		}, "A").start();

		new Thread(() -> {
			threadLocal.set("Thread B's data");
			print();
			threadLocal.remove();
		}, "B").start();
	}

	static void print() {
		System.out.println(Thread.currentThread() + " : " + threadLocal.get());
	}
}
