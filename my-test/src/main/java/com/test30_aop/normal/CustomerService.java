package com.test30_aop.normal;

/**
 * @Auther: weiliang
 * @Date: 2020/12/26 12:16
 * @Description:
 */
public class CustomerService {

	private String name;
	private String url;

	public void setName(String name) {
		this.name = name;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 *
	 */
	public void printName(){
		System.out.println("Customer name : " + this.name);
	}

	/**
	 *
	 */
	public void printURL(){
		System.out.println("Customer website : " + this.url);
	}

	/**
	 *
	 */
	public void printThrowException(){
		System.out.println("throw exception");
		throw new IllegalArgumentException();
	}

}
