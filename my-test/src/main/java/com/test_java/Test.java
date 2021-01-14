package com.test_java;

import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Auther: weiliang
 * @Date: 2021/1/7 10:19
 * @Description:
 */
public class Test {

	public static void main(String[] args) {
		ReentrantLock lock = new ReentrantLock(true);
		lock.lock();
		System.out.println("lock");
		lock.unlock();

		LockSupport.park();
	}
}
