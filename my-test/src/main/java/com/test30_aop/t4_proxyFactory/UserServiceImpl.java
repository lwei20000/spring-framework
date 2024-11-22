package com.test30_aop.t4_proxyFactory;

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
