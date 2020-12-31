package com.my;

import com.my.app.AppConfig;
import com.my.dao.CityMapper;
import com.my.service.CityService;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Auther: weiliang
 * @Date: 2020/12/31 20:58
 * @Description:
 */
public class TestNormal {
	public static void main(String[] args) {
		SqlSessionFactory sqlSessionFactory = null;
		SqlSession session = sqlSessionFactory.openSession();
		CityMapper cityMapper = session.getMapper(CityMapper.class);
		cityMapper.query();
	}
}
