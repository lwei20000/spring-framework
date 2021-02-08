package com.test_shangguigu.t05_conditional.config;


import com.test_shangguigu.t02_componentScan.beans.Persion;
import com.test_shangguigu.t05_conditional.condition.LinuxConditon;
import com.test_shangguigu.t05_conditional.condition.MacConditon;
import com.test_shangguigu.t05_conditional.condition.WindowsConditon;
import org.springframework.context.annotation.*;


@Configuration
public class AppConfig05 {

	@Scope("singleton")
	@Bean("person")
	@Lazy
	public Persion persion(){
		System.out.println("给容器中添加bean。。。");
		return new Persion("zhangsan", 20);
	}

	/**
	 * 如果系统是window就给容器中注册bill
	 * @return
	 */
	@Bean("bill")
	@Conditional({WindowsConditon.class})
	public Persion persion01(){
		return new Persion("bill gates", 60);
	}

	/**
	 * 如果系统是linux就给容器中注册linus
	 * @return
	 */
	@Bean("linus")
	@Conditional({LinuxConditon.class})
	public Persion persion02(){
		return new Persion("linus", 50);
	}

	/**
	 * 如果系统是linux就给容器中注册linus
	 * @return
	 */
	@Bean("jobs")
	@Conditional({MacConditon.class})
	public Persion persion03(){
		return new Persion("jobs", 50);
	}

}
