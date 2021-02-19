package com.test_shangguigu.t41_ext.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 扩展原理：
 * BeanPostProcessor：bean后置处理器，bean创建对象舒适化前后进行拦截工作的
 * BeanFactoryPostProcessor：beanFactory后置处理器，在BeqnFactory标准初始化之后调用（所有的Bean定义被加载了，但是还没有Bean被初始化）
 *
 * 1、BeanFactoryPostProcessor 原理：
 *     1)ioc容器创建对象
 *     2）invokebeanFactoryPostProcessor(beanFactory); 执行BeanFactoryPostProcessor
 *        如何找到所有的BeanFactoryPostProcessor并执行他们的方法：
 *            1）直接在BeanFacotry中找到所有的类型是BeanFactoryPostProcessor的组件，并执行他们的方法。
 *            2）在初始化创建其它组件前面执行。
 *
 * 2、BeanDefinitionRegistryPostProcessor ： 它是BeanFactoryPostProcessor 的子接口
 *    postProcessBeanDefinitionRegistry();
 *    在所有bean定义信息将要被加载，bean实例还未创建的时候执行。
 *    它优先于BeanFactoryPostProcessor执行：利用BeanDefinitionRegistryPostProcessor可以给容器中再额外添加一些组件
 *
 *    原理：
 *        1）ioc创建对象
 *        2）refresh() -> invokeBeanFactoryPostProcessors(beanFactory); // 跟上面一样
 *        3）先从容器中获取到所有的BeanDefinitionRegistryPostProcessor组件，
 *            1）依次出发所有的postProcessBeanDefinitionRegistry。
 *            2）再来出发postProcessBeanFactory方法（普通BeanFactoryPostProcessor的方法）
 *        4）再来从容器中找到BeanFactoryPostProcessor，然后依次出发postProcessBeanFactory方法
 *
 * 3、ApplicationListener ： 舰艇容器中发生的事件，事件驱动模型开发。
 *     public interface ApplicationListener<E extends ApplicationEvent>
 *         监听ApplicationEvent 以及其下面的子类
 *
 *    步骤：
 *        1）写一个监听器来监听某个事件（ApplicationEvent及其子类）
 *        2）把监听器加入到容器
 *        3）只要容器中有相关事件发布，我们就能监听到这个事件：
 *           例如：一下两个是Spring自己发布的事件
 *           ContextRefreshedEvent ： 容器刷新完成事件
 *           ContextClosedEvent ： 容器管理事件
 *        3）如何发布一个事件？参看IocTest
 *
 *    原理：
 *        ContextRefreshedEvent // 容器刷新事件
 *        IocTest$1 // 自定义事件
 *        ContextClosedEvent // 容器关闭事件
 *
 *        1）ContextRefreshedEvent事件
 *            1）容器创建对象：refresh()
 *            2)finishRefresh();容器刷新完成会发布ContextRefreshEvent事件
 *        2）自己发布的事件
 *        3）ContextClosedEvent容器关闭事件
 *
 *       【事件发布流程】
 *            publishEvent(new ContextRefreshedEvent(this));
 *               事件发布就成：
 *                   1）获取事件的多播器（事件广播器）
 *                   2）multicastEvent派发事件
 *                   3）获取到所有的ApplicationListener
 *                       1）如果有Executor 可以支持使用Executor进行一步派发
 *                          Executor executor = getTaskExecutor();
 *                       2) 否则，同步的方式直接执行派发。
 *
 *      【事件的派发器】
 *       1）容器创建对象：refresh()
 *       2）initApplicationEventMulticaster(); 初始化ApplicationEventMulticaster
 *           1）先去容器中找有没有 id = applicationEventMulticaster 的组件
 *           2）如果没有，就自己创建一个。this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
 *               并且加入到容器中，我们就可以在其它组件要派发事件，自动注入这个applicationEventMulticaster；
 *
 *      【容器中有哪些监听器】
 *       1）容器创建对象：refresh()
 *       2）registerListeners();
 *           从容器中拿到所有的监听器，把他们注册到applicationEventMulticaster中
 *           String[] listernerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
 *
 *      【注解@EventListener的原理】
 *       例子：@EventListener(classes={ApplicationEvent.class})】
 *       原理：使用EventListenerMethodProcessor处理器来解析方法上的@EventListener
 */
@ComponentScan("com.test_shangguigu.t41_ext")
@Configuration
public class AppConfig41 {

}
