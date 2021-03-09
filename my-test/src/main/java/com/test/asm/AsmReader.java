package com.test.asm;

//import jdk.internal.org.objectweb.asm.ClassReader;
//import jdk.internal.org.objectweb.asm.util.TraceClassVisitor;
//import java.io.IOException;
//import java.io.PrintWriter;
//
//public class AsmReader {
//	public static void main(String[] args) throws IOException {
//		ClassReader reader = new ClassReader("java.lang.String");                        // 读取java.lang.String类
//		TraceClassVisitor visit = new TraceClassVisitor(new PrintWriter(System.out));    //
//		reader.accept(visit,0);
//	}
//}