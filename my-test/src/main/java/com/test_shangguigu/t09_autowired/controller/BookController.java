package com.test_shangguigu.t09_autowired.controller;

import com.test_shangguigu.t09_autowired.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class BookController {

	@Autowired
	private BookService bookService;
}
