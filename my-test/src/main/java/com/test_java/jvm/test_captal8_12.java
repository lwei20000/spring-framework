package com.test_java.jvm;

import static java.lang.invoke.MethodHandles.lookup;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class test_captal8_12 {

	static class ClassA {
		public void println(String s) {
			System.out.println("s");
		}
	}


	public static void main(String[] args) throws Throwable {
		Object obj = System.currentTimeMillis() % 2 == 0? System.out : new ClassA();
		getPrintMH(obj).invokeExact("hello");

	}

	private static MethodHandle getPrintMH(Object receiver) throws Throwable {
		MethodType mt = MethodType.methodType(void.class, String.class);
		return lookup().findVirtual(receiver.getClass(), "println",mt).bindTo(receiver);

	}

}
