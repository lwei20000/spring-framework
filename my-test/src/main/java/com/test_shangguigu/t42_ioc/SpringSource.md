Spring容器的refresh()
1、prepareRefresh()刷新前的预处理；
    1）initPropertySource(); 初始化一些属性设置；空的，留给子类实现。
    2）getEnvironment().validateRequiredProperties() 校验属性的合法性。
    3）earlyApplicationEvents = new LinkedHashSet<ApplicationEvent>(); 保存容器中的一些早期的事件；
2、obtainFreshBeanFactory() 获取BeanFactory
    1）refreshBeanFactory();刷新bean工厂
        创建一个this.beanFactory = new DefaultListableBeanFactory();
        设置id
    2）getBeanFactory(); 返回刚才GenericApplicationContext创建的BeanFactory
    3）将创建的BeanFactory【DefaultListableBeanFactory】返回；
3、postProcessBeanFactory(beanFactory); BeanFactory的预准备工作（BeanFactory进行一些设置）
    1）设置BeanFactory的类加载器，支持表达式解析器。。。
    2）添加部分BeanPostProcessor【ApplicationContextAwareProcessor】
    3）设置忽略的自动装配的接口 EnviromentAware、EmbeddedValueResolverAware、xxx
    4）注册可以解析的自动装配；我们能直接在任何组件中自动注入：BeanFactory、ResourceLoader、ApplicationEventPublisher 
    5）添加BeanPostProcessor（ApplicationListenerDetector）
    6）添加编译时的AspectJ    
    7）给BeanFactory中注册一些能用的组件：    
       enriroment【ConfigurableEnviroment】
       systemProperties【Map<String, Object>】
       systemEnviroment【Map<String,Object>】
4、postProcessBeanFactory(beanFactory); BeanFactory准备工作完成后进行的后置处理工作；
    1）子类通过充血这个方法来在BeanFactory创建并准备完成以后做进一步的设置。
====================以上是BeanFactory的创建和预准备工作=========================== 
5、invokeBeanFactoryPostProcessors(beanFactory); 执行BeanFactoryPostProcessor；
    BeanFactoryPostProcessor=BeanFactory的后置处理器。在BeanFactory标准初始化之后执行的。
    两个接口：BeanFactoryPostProcessor、BeanDefinitionRegistryPostProcessor
    1）执行BeanFactoryPostProcessor的方法
        先执行【BeanDefinitionRegistryPostProcessor】
        1）获取所有BeanDefinitionRegistryPostProcessor
        2）看先执行实现了PriorityOrdered优先级接口的BeanDefinitionRegistryPostProcessor
           postProcessor.postProcessBeanDefinitionRegistry(registry)
        3) 再执行实现了Ordered顺序接口的BeanDefinitionRegistryPostProcessor
        4） 最后，执行所有没有实现任何接口的BeanDefinitionRegistryPostProcessor
        
        再执行【BeanFactoryPostProcessor】
        1）获取所有 BeanFactoryPostProcessor
        2）看先执行实现了PriorityOrdered优先级接口的 BeanFactoryPostProcessor
           postProcessor.postProcessBeanFactory(registry)
        3) 再执行实现了Ordered顺序接口的 BeanFactoryPostProcessor
        4） 最后，执行所有没有实现任何接口的 BeanFactoryPostProcessor
6、registerBeanPostProcessors(beanFactory); 注册bean的后置处理器；后置处理器是用来拦截bean的创建过程的。
    不同接口类型的BeanPostProcessor，在Bean创建前后的执行时机是不一样的
    BeanPostProcessor：DestructionAwareBeanPostProcessor
                       InstantiationAwareBeanPostProcessor
                       SmartInstantiationAwareBeanPostProcessor
                       MergedBeanDefinitonPostProcessor
    1）获取所有的BeanPostProcessor；后置处理器都默认可以有PriorityOrdered，Ordered两个接口来指定优先级。
    2）先注册PriorityOrdered优先级接口的BeanPostProcessor
       把每一个BeanPostProcessor添加到BeanFactory
    3）再注册实现了Ordered接口的BeanPostProcessor
    4）最后注册没有实现任何接口的BeanPostProcessor
    5）最最后注册MergedBeanDefinitionPostProcessor
    6）注册一个ApplicationListenerDetector；来在Bean创建完成后检查是否是ApplicationListener，
       如果是就放在容器中保存起来。
7、initMessageSource(); 初始化MessageSource组件（做国际化功能，消息绑定，消息解析）
    1）获取BeanFactory
    2）看容器中时候有id=messageSource的组件，
       如果没有就创建一个DelegatingMessageSource
       ps：MessageSource一般用于取出国际化配置文件中某个key的值，能按照区域信息取得。
    3）把创建好的MessageSource注册在容器中，以后获取国际化配置文件的值的时候，可以自动注册MessageSource
       beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
       MessageSource.getMesage(String code, Object[] args, String defaultMessage,)
8、initApplicationEventMulticaster();
    1）获取BeanFactory
    2）从BeanFactory中获取applicationeventMulticaster的ApplicationEventMulticaster
    3）如果上一步没有配置，创建一个SimpleApplicationEventMulticaster
    4）将创建的ApplicationEventMulticaster添加到BeanFactory中，以后其它组件直接自动注入。
9、onRefresh(); 留给子容器（子类）
    1）子类充血这个方法，在容器刷新的时候可以自定义逻辑。
10、registerListeners(); 给容器中将所有项目里面的ApplicationLonListener注册进来。
    1）从容器中拿到所有的ApplicationListener
    2）将每个监听器添加到事件派发起中：
        getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
    3)派发之前步骤产生的事件；
11、finishBeanFactoryInitialization(beanFactory); 初始化所有剩下的单实例bean
    1）beanFactory.preInstantiateSingletons(); 初始化剩下的单实例bean
        1）获取容器中的所有Bean，一次进行初始化
        2）获取bean的定义信息，RootBeanDefinition
        3）不是抽象的并且是单实例的，并且不是懒加载的===>创建Bean，getBean()
            1）判断是FactoryBean（是否是实现FactoryBean接口的Bean）
            2）若不是工厂Bean，利用getBean(beanName)创建对象
                0 getBean(beanName)；
                1 doGetBean(name,null,null,false);
                2 先获取缓存中保存的单实例Bean，如果能获取到说明这个Bean之前被创建过。（所有被创建过的单实例bean都会被缓存起来）
                    /** Cache of singleton objects: bean name to bean instance. */
                  	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
                3 缓存中获取不到，开始Bean的创建对象流程
                4 标记当前bean已经被创建
                5 获取Bean的定义信息
                6 获取当前Bean依赖的其它bean信息；如果有依赖其它bean，那么getBean()的方式先创建出来。
                7 启动单实例bean的创建流程
                   1）createBean(beanName,mbd,args);
                   2) Object bean = resolveBeforeInstantiation(beanName,mbdToUse); 
                      InstantiationAwareBeanPostProcessor 提前拦截返回代理对象:-----------【后置处理器】
                      先触发 postProcessBeforeInstantiation();
                      再触发 postProcessAfterInitialization();
                   3) 如果前面的 InstantiationAwareBeanPostProcessor 没有返回代理对象，调用4） ；
                   4) Object beanInstance = doCreatebean(beanName,mdbToUse,args); 
                       1）【创建bean实例】 createbeanInstatnce(beanName,mbd,args);
                          利用工厂方法或者对象的构造器创建出Bean实例
                       2）applyMergedBeanDefinitionPostProcessor
                          调用MergedBeanDefinitionPostProcessor的postProcessMergedBeanDefinition  ----【后置处理器一】
                       3）【bean属性赋值】populatebean(beanName,mbd,instanceWrapper);
                           <------赋值之前------->
                           1 拿到InstantiationAwarePostProcessor后置处理器
                             postProcessAfterInstantiation()
                           2 拿到InstantiationAwarePostProcessor后置处理器
                             postProcessPropertyValue()
                           <------属性赋值------>
                           3 应用Bean属性的值，为属性利用setter方法进行赋值
                             applyPropertyValues(beanName,mbd,bw,pvs);
                       4）【bean初始化】initializeBean(beanName,exposedObject,mbd);
                           1 【执行Aware接口】invokeAwareMethods(beanName,bean); 执行xxxAware接口的方法
                           2 【执行后置处理器初始化之前方法】applyBeanPostProcessorBeforeInitialization(wrappedBean,beanName,)  
                              BeanPostProcessor.postProcessBeforeInitialization();
                           3 【执行初始化方法】invokeInitMethods(beanName,wrappedBean,mbd);
                              1）是否是InitializingBean接口的实现，执行接口规定的初始化
                              2）是否自定义初始化，有的话执行自定义初始化方法。
                           4 【执行后置处理器初始化之后方法】applyBeanPostProcessorAfterInitialization
                              BeanPostProcessor.postProcessAfterInitialization();
                       5) 注册bean的销毁方法
                   5）将创建好的bean添加到缓存中singletonObjects；
                      ioc容器就是这些Map，很多的Map里面保存了单实例bean，环境信息。。。。
    2）所有Bean都利用getBean()创建完成之后，
       检查所有的bean是否是SmartInitializingSingleton接口的；如果是，就执行afterSingletonsInstantiated()方法。

12、finishRefresh(); 完成BeanFactory的初始化工作，IOC容器就创建完成；
    1）initLifecycleProcessor()； 初始化和生命周期有关的后置处理器，
       默认从容器中找是否有lifecycleProcessor的组件【Li非常有才了P肉测试送人】，如果没有就创建new DefaultLifecycleProcessor()；
       加入到容器中。
       
       写一个LifecycleProcessor的实现类，可以在BeanFactory的
         onRefresh()
         onClose()
         
    2）getLifecycleProcessor().onRefresh();
       拿到前面定义的生命周期处理器，回调onRefresh();
    3) publishEvent(new ContextRefreshedEvent(this));发布容器刷新完成事件。
    4）liveBeanView.registerApplicationContext(this);
       
【总结】
1、Spring容器在启动的时候，贤惠保存所有注册进来的Bean的定义信息，
    1）xml注册bean：<bean>
    2) 注解注册Bean：@Service、@Component、@Bean、xxx
2、Spring容器会创建这些Bean。
   创建时机：
   1）用到这个Bean的时候，利用getBean方法创建bean，创建好以后保存在容器中。
   2）统一创建剩下所有bean的时候。finishBeanFactoryInitialization();
   3) 后置处理器BeanPostProcessor：
      1）个bean创建完成，都会使用各种后置处理器进行处理，来增强bean的功能。
          AutowiredAnnotationbeanPostProcessor：处理自动注入
          AnnotationAwareAspectJAutoProxyCreator：来做AOP功能
          xxx。。。
          增强的功能注解：
          AsynAnnotationBeanPostProcessor：异步注解
          。。。
   4) 事件驱动模型
      ApplicationListener：事件监听
      ApplicationEventMulticaster：事件派发