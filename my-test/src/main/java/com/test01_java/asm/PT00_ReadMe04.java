package com.test01_java.asm;

public class PT00_ReadMe04 {
	public static void main(String[] args) {
		int i = 2;
		i = i++;
		System.out.println(i);
		new PT00_ReadMe04().test("a", "b", "c"+1, "d");
	}

	public void test(String a, String b, String c, String d) {}
}
