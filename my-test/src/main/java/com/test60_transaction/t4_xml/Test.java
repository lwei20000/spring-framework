package com.test60_transaction.t4_xml;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 使用Spring事务的方式一：配置文件最标准配置
 * @Auther: weiliang
 * @Date: 2024/11/21 09:57
 * @Description:
 */
public class Test {
	public static void main(String[] arg) {
		ApplicationContext context = new ClassPathXmlApplicationContext("com/test60_transaction_01.xml");
		NewsDao dao = context.getBean("newsDaoTransProxy",NewsDao.class);
		dao.insert("新闻标题","新闻内容");
	}
}
