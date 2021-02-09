package com.test_shangguigu.t09_autowired.service;

import com.test_shangguigu.t09_autowired.dao.BookDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.annotation.Inherited;

@Service
public class BookService {

	//@Autowired
	//@Resource
	private BookDao bookDao;

	@Override
	public String toString() {
		return "BookService [bookDao=" + bookDao + "]";
	}
}
