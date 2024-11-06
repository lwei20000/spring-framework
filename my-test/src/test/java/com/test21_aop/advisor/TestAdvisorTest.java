package com.test21_aop.advisor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Auther: weiliang
 * @Date: 2020/12/25 09:58
 * @Description:
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:com/aop/advisor/applicationContext.xml"})
public class TestAdvisorTest {

	@Autowired
	public TestPoint testPoint;

	@Test
	public void test() {
		testPoint.test();
	}
}
