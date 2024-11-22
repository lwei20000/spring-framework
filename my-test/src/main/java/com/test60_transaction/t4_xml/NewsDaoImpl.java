package com.test60_transaction.t4_xml;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @Auther: weiliang
 * @Date: 2024/11/21 09:55
 * @Description:
 */
public class NewsDaoImpl implements NewsDao {

	private org.apache.tomcat.jdbc.pool.DataSource dataSource;

	// private DataSource dataSource;
	public void setDataSource(org.apache.tomcat.jdbc.pool.DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void insert(String title, String content) {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		template.update("insert into t_news values ('新闻标题1','内容1')");
		//两次相同的操作，将违反主键约束
		template.update("insert into t_news values ('新闻标题2','内容2')");
	}
}
