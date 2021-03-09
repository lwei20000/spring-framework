package com.test_java.threadlocal;

public class MyDemo01 {
	private String content;
	private String getContent() {
		return content;
	}
	private void setContent(String content) {
		this.content = content;
	}
	public static void main(String[] args) {
		final MyDemo01 demo = new MyDemo01();
		for (int i = 0; i < 5; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					demo.setContent(Thread.currentThread().getName()+"的数据");
					//System.out.println("-----");
					System.out.println(Thread.currentThread().getName() + "--->" +demo.getContent());
				}
			});
			thread.setName("线程" +i);
			thread.start();
		}
	}
}
