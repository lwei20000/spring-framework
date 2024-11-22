package com.test60_transaction.t2_programmatic;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @Auther: weiliang
 * @Date: 2024/11/22 15:29
 * @Description:
 */
public class Test {
	public static void main(String[] args) {
		//定义一个数据源
		DataSource dataSource = new DataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/springCloudTestDB?characterEncoding=UTF-8&serverTimezone=UTC");
		dataSource.setUsername("root");
		dataSource.setPassword("111111.qq");
		dataSource.setInitialSize(5);
		//定义一个JdbcTemplate，用来方便执行数据库增删改查
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		//1.定义事务管理器，给其指定一个数据源（可以把事务管理器想象为一个人，这个人来负责事务的控制操作）
		PlatformTransactionManager platformTransactionManager = new DataSourceTransactionManager(dataSource);
		//2.定义事务属性：TransactionDefinition，
		// TransactionDefinition可以用来配置事务的属性信息，比如事务隔离级别、事务超时时间、事务传播方式、是否是只读事务等等。
		TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		//3.开启事务：调用platformTransactionManager.getTransaction开启事务操作，得到事务状态(TransactionStatus)对象
		TransactionStatus transactionStatus = platformTransactionManager.getTransaction(transactionDefinition);
		//4.执行业务操作，下面就执行2个插入操作
		try {
			System.out.println("before:" + jdbcTemplate.queryForList("SELECT * from t_news"));

			jdbcTemplate.update("insert into t_news (title, content) values (?,?)", "test1-1","test1-1");
			jdbcTemplate.update("insert into t_news (title, content) values (?,?)", "test1-2","test1-1");

			//5.提交事务：platformTransactionManager.commit
			platformTransactionManager.commit(transactionStatus);
		} catch (Exception e) {
			//6.回滚事务：platformTransactionManager.rollback
			platformTransactionManager.rollback(transactionStatus);
		}
		System.out.println("after:" + jdbcTemplate.queryForList("SELECT * from t_news"));
	}
}
