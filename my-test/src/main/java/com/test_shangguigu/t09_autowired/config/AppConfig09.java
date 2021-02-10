package com.test_shangguigu.t09_autowired.config;

import com.test_shangguigu.t09_autowired.beans.Person;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 自动装配：
 *     Spring利用依赖主妇DI，完成对IOC容器中各个组件的依赖关系赋值
 *
 * 1）@Autowired 自动注入 【这是spring框架的注解】
 *     1）默认有限按照类型去容器中找到对应的组件：applicationContext.getBean(BookDao.class);
 *     2) 如果找到多个相同类型的组件，再将属性的名称作为组件的id去容器中查找 application.getBean("bookDao")
 *     3）@Qualifier("bookDao")=明确指定。使用Qualifier指定需要装配的组件的id，而不是使用属性名。
 *     【容器中没有的组件，如何自动装配？】
 *     4）自动装配默认一定要将属性赋值好，没有就会报错。能否设定为如果找得到就装配，如果没找到不装配呢？答：可以。@Autowired(required=false)
 *     5) @Primary =首选装配。 让Spring进行自动装配的时候，默认使用首选的bean，
 *                  也可以继续使用@Qualifier指定需要装配的bean的名字。
 *
 * 2）Spring还支持使用@Resource JSR250 和@Inject JSR330 【这是java规范的注解】
 *     1）@Resource
 *            它可以和@Autowired一样进行实现自动装配功能，默认是按照组件名进行自动装配的。
 *            它没有支持@Primary工能，没有支持@Autowired(required=false)
 *     2)@Inject
 *            需要导入javax.inject包，和@Autowired功能一样。
 *            它没有支持@Autowired(required=false)
 *
 * 3）@Autowired：构造器、参数、方法、属性
 *     1）【标注在方法位置】@Bean+方法参数，参数从容器中获取；默认不屑@Autowired效果是一样的，都能自动装配
 *     2）【标注在构造器上】如果组件只有一个有参构造器，这个有参构造器的@@Autowired可以省略，参数位置的组件还是可以自动从容器中获取
 *     3）【标注在参数位置】
 *
 * 3）自定义组件想要使用Spring容器底层的一些组件（ApplicationContext，BeanFactory，xxx），
 *    自定义组件实现xxxAware，在创建对象的时候，会调用接口规定的方法注入组件需要的组件。
 *    Aware吧Spring底层一些组件注入到自定义的Bean中；
 *    XXXAware，功能使用xxxProcessor
 *        ApplicationContextAware==》ApplicationContextAwareProcessor
 *
 *
 *
 *
 */
@Configuration
@ComponentScan({"com.test_shangguigu.t09_autowired.controller"
		,"com.test_shangguigu.t09_autowired.service"
		,"com.test_shangguigu.t09_autowired.dao"
		,"com.test_shangguigu.t09_autowired.beans"
})
public class AppConfig09 {

















}
