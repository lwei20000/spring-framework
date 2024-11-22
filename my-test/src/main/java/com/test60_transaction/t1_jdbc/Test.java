package com.test60_transaction.t1_jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 单独使用JDBC的connetcion实现事务管理
 */
public class Test {
	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {

			// 获取与数据库的链接
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/springCloudTestDB", "root", "111111.qq");
			conn.setAutoCommit(false);//默认自动提交，现在改为false手动提交

			// 创建代表SQL语句的对象(建议用PrepareStatement)
			stmt = conn.prepareStatement("SELECT * from t_news");
			// 执行SQL语句
			stmt.executeUpdate();//根据不同的操作 增删改查 运用不同的方法

			// 在此设置回滚点当出现异常时操作则回滚到这个点
			// Savepoint savepoint = conn.setSavepoint();//如果没有设置则回滚到第一条sql语句执行之前
			stmt = conn.prepareStatement("SELECT * from t_news");
			// 执行SQL语句
			stmt.executeUpdate();

			// 提交事务
			conn.commit();
		} catch (Exception e) {
			// TODO: handle exception
			if (conn != null) {
				try {
					//rollback
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			if (conn != null) {
				try {
					conn.commit();

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				stmt.close();
				conn.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}

		}
	}
}
