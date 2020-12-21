import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.logging.Logger;

/**
 * @Auther: weiliang
 * @Date: 2020/12/15 17:54
 * @Description:
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-public.xml" })
public class test {
	@Resource
	private PlatformTransactionManager txManager;
	@Resource
	private DataSource dataSource;
	private static JdbcTemplate jdbcTemplate;
	Logger logger= Logger.getLogger(String.valueOf(test.class));
	private static final String INSERT_SQL = "insert into testtranstation(sd) values(?)";
	private static final String COUNT_SQL = "select count(*) from testtranstation";
	@Test
	public void testdelivery(){
		//定义事务隔离级别，传播行为，
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		//事务状态类，通过PlatformTransactionManager的getTransaction方法根据事务定义获取；获取事务状态后，Spring根据传播行为来决定如何开启事务
		TransactionStatus status = txManager.getTransaction(def);
		jdbcTemplate = new JdbcTemplate(dataSource);
		int i = jdbcTemplate.queryForList(COUNT_SQL).size();
		System.out.println("表中记录总数："+i);
		try {
			jdbcTemplate.update(INSERT_SQL, "1");
			txManager.commit(status);  //提交status中绑定的事务
		} catch (RuntimeException e) {
			txManager.rollback(status);  //回滚
		}
		i = jdbcTemplate.queryForList(COUNT_SQL).size();
		System.out.println("表中记录总数："+i);
	}
}
