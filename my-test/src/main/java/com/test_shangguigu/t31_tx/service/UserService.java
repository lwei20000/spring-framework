package com.test_shangguigu.t31_tx.service;

import com.test_shangguigu.t31_tx.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	@Autowired
	private UserDao userDao;

	//@Transactional
	public void insertUser() throws Exception {
		userDao.insert();
		System.out.println("插入完成。。。");
		//int i = 12/0;
		//throw new Exception();
	}
}