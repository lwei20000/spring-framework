package com.test01_java.aqs;

import java.util.concurrent.locks.Lock;

/**
 * https://www.bilibili.com/video/BV1Dh411R77m
 */
public class MyReentrantLockTest {
	private int num = 0;
	private int aNum = 0;

	private Lock lock = new MyLock();

	public void addNum() {
		lock.lock();
		try {
			num++;
			// 调用另外一个需要锁的方法，测试是否能重入
			a1();
		} finally {
			lock.unlock();
		}
	}

	public void a1() {
		lock.lock();
		try{
			aNum++;
		} finally {
			lock.unlock();
		}
	}

	public int getNum() {
		return num;
	}

	public static void main(String[] args) {
		MyLockTest t = new MyLockTest();

		for (int i = 0; i < 10; i++) {
			new Thread(() -> {
				for (int j = 0; j < 100; j++) {
					t.addNum();
					try {
						Thread.sleep(1L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		for (int i = 0; i < 11; i++) {
			System.out.println("now num====" + t.getNum());
			try{
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
