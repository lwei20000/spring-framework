package com.test21_aop.springaoptest;

/**
 * @Auther: weiliang
 * @Date: 2020/12/14 10:09
 * @Description:
 */
public class UserServiceImpl implements UserService{

	@Override
	public void updateUser() {
		System.out.println("$$$$$$执行业务逻辑$$$$$");
	}
}
