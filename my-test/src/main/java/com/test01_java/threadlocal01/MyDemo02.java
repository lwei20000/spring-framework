package com.test01_java.threadlocal01;

public class MyDemo02 {
	ThreadLocal<String> t1 = new ThreadLocal<String>();
	private String getContent() {
		return t1.get();
	}
	private void setContent(String content) {
		t1.set(content);
	}
	public static void main(String[] args) {
		final MyDemo02 demo = new MyDemo02();
		for (int i = 0; i < 5; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					demo.setContent(Thread.currentThread().getName()+"的数据");
					//System.out.println("-----------------");
					System.out.println(Thread.currentThread().getName() + "--->" +demo.getContent());
				}
			});
			thread.setName("线程" +i);
			thread.start();
		}
	}
}
