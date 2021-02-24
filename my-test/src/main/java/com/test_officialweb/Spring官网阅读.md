[toc]





# Spring官网阅读

~~~java
// 个人主页
https://daimingzhi.blog.csdn.net

// Spring官网读书笔记
https://blog.csdn.net/qq_41907991/category_9601507.html

// Spring源码解析
https://blog.csdn.net/qq_41907991/category_9907747.html
~~~

## Spring官网阅读（一）容器及实例化

### Spring容器

#### 容器是什么？

我们先看官网中的一句话：

> 1.2  Container Overview
>
> The `org.springframework.context.ApplicationContext` interface represents the Spring IoC container and is responsible for instantiating, configuring, and assembling the beans.

翻译下来大概就是：

1. Spring IOC容器就是一个`org.springframework.context.ApplicationContext`的实例化对象
2. 容器负责了实例化，配置以及装配一个bean

那么我们可以说：

- **从代码层次来看：Spring容器就是一个实现了`ApplicationContext`接口的对象**，
- **从功能上来看： Spring 容器是 Spring 框架的核心，是用来管理对象的。容器将创建对象，把它们连接在一起，配置它们，并管理他们的整个生命周期从创建到销毁。**

#### 容器如何工作？

我们直接看官网上的一张图片，如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235039696.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

**Spring容器通过我们提交的pojo类以及配置元数据产生一个充分配置的可以使用的系统**

这里说的配置元数据，实际上我们就是我们提供的XML配置文件，或者通过注解方式提供的一些配置信息

### Spring Bean

#### 如何实例化一个Bean？

从官网上来看（1.3.2的目录），主要有以下三种方法

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235049718.jpg)

1. 构造方法
2. 通过静态工厂方法
3. 通过实例工厂方法

这三种例子，官网都有具体的演示，这里就不再贴了，我们通过自己查阅部分源码，来验证我们在官网得到的结论，然后通过debug等方式进行验证。

我们再从代码的角度进行一波分析，这里我们直接定位到`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBeanInstance`这个方法中，具体定位步骤不再演示了，大家可以通过形如下面这段代码：

```java
// 这里我们通过xml配置实例化一个容器
ClassPathXmlApplicationContext cc = new ClassPathXmlApplicationContext("classpath:application.xml");
MyServiceImpl luBan = (MyServiceImpl) cc.getBean("myServiceImpl");
```

直接main方法运行，然后在`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBeanInstance`这个方法的入口打一个断点，如图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235117877.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235129944.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

接下来我们对这个方法进行分析，代码如下：

```java
protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
		// 1.获取这个bean的class属性，确保beanDefinition中beanClass属性已经完成解析
    // 我们通过xml从<bean>标签中解析出来的class属性在刚刚开始的时候必定是个字符串
		Class<?> beanClass = resolveBeanClass(mbd, beanName);

    if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
			  throw new BeanCreationException(mbd.getResourceDescription(), beanName,
					"Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
		}
    	
    // 2.通过beanDefinition中的supplier实例化这个bean
		Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
		if (instanceSupplier != null) {
			return obtainFromSupplier(instanceSupplier, beanName);
		}
		
    // 3.通过FactoryMethod实例化这个bean
		if (mbd.getFactoryMethodName() != null) {
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}

    // 4.下面这段代码都是在通过构造函数实例化这个Bean,分两种情况，一种是通过默认的无参构造，一种是通过推断出来的构造函数
		boolean resolved = false;
		boolean autowireNecessary = false;
		if (args == null) {
			synchronized (mbd.constructorArgumentLock) {
				if (mbd.resolvedConstructorOrFactoryMethod != null) {
					resolved = true;
					autowireNecessary = mbd.constructorArgumentsResolved;
				}
			}
		}
   
		if (resolved) {
			if (autowireNecessary) {
				return autowireConstructor(beanName, mbd, null, null);
			}
			else {
				return instantiateBean(beanName, mbd);
			}
		}

		// Candidate constructors for autowiring?
		Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
		if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
				mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
			return autowireConstructor(beanName, mbd, ctors, args);
		}

		// Preferred constructors for default construction?
		ctors = mbd.getPreferredConstructors();
		if (ctors != null) {
			return autowireConstructor(beanName, mbd, ctors, null);
		}

		// No special handling: simply use no-arg constructor.
		return instantiateBean(beanName, mbd);
	}
```

我们主要关注进行实例化的几个方法：

1. 通过`BeanDefinition`中的`instanceSupplier`直接获取一个实例化的对象。这个`instanceSupplier`属性我本身不是特别理解，在xml中的标签以及注解的方式都没有找到方式配置这个属性。后来在`org.springframework.context.support.GenericApplicationContext`这个类中找到了以下两个方法

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235146473.png)

经过断点测试，发现这种情况下，在实例化对象时会进入上面的supplier方法。下面是测试代码：

```java
public static void main(String[] args) {
    // AnnotationConfigApplicationContext是GenericApplicationContext的一个子类
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
		ac.registerBean("service", Service.class,Service::new);
		ac.refresh();
		System.out.println(ac.getBean("service"));
	}
```

可以发现进入了这个方法进行实例化

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235200889.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

这个方法一般不常用，平常我们也使用不到，就不做过多探究，笔者认为，这应该是Spring提供的一种方便外部扩展的手段，让开发者能够更加灵活的实例化一个bean。

1. 接下来我们通过不同的创建bean的手段，来分别验证对象的实例化方法

- 通过`@compent`,`@Service`等注解的方式

测试代码：

```java
public class Main {
	public static void main(String[] args) {
    // 通过配置类扫描
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(Config.class);
		System.out.println(ac.getBean(Service.class));
	}
}
```

```java
@Component
public class Service {
}
```

观察debug:

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235214406.jpg)

可以发现，代码执行到最后一行，同时我们看代码上面的注释可以知道，当没有进行特殊的处理的时候，默认会使用无参构造函数进行对象的实例化

- 通过普通XML的方式（同`@component`注解，这里就不赘诉了）
- 通过`@Configuration`注解的方式

测试代码：

```java
public class Main {
	public static void main(String[] args) {
    // 通过配置类扫描
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(Config.class);
    // 这里将测试对象换为config即可，同时记得将条件断点更改为beanName.equlas("config")
		System.out.println(ac.getBean(config.class));
	}
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235332887.jpg)

同样，断点也进入最后一行

- 通过`@Bean`的方式

测试代码：

```java
@Configuration
@ComponentScan("com.dmz.official")
public class Config {
    @Bean
    public Service service(){
        return new Service();
    }
}

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(Config.class);
        System.out.println(ac.getBean("service"));
    }
}
```

断点结果：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235341477.jpg)

可以发现，通过`@Bean`方法创建对象时，Spring底层是通过`factoryMethod`的方法进行实例化对象的。Spring会在我们需要实例化的这个对象对应的`BeanDefinition`中记录`factoryBeanName`是什么（在上面的例子中factoryBeanName就是config）,同时会记录这个factoryBean中创建对象的`factoryMethodName`是什么，最后通过`factoryBeanName`获取一个Bean然后反射调用`factoryMethod`实例化一个对象。

这里我们需要注意几个概念：

1. 这里所说的通过静态工厂方式通过`factoryBeanName`获取一个Bean，注意，这个Bean，不是一个`FactoryBean`。也就是说不是一个实现了`org.springframework.beans.factory.FactoryBean`接口的Bean。至于什么是`FactoryBean`我们在后面的文章会认真分析
2. 提到了一个概念`BeanDefinition`，它就是Spring对自己所管理的Bean的一个抽象。不懂可以暂且跳过，后面有文章会讲到。

- 通过静态工厂方法的方式

测试代码：

```java
public static void main(String[] args) {
    ClassPathXmlApplicationContext cc = new ClassPathXmlApplicationContext("application.xml");
    System.out.println(cc.getBean("service"));
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
<!--	<bean id="myServiceImpl" class="com.dmz.official.service.Service"/>-->

	<!-- the factory bean, which contains a method called get() -->
	<bean id="myFactoryBean" class="com.dmz.official.service.MyFactoryBean">
		<!-- inject any dependencies required by this locator bean -->
	</bean>

	<!-- 测试实例工厂方法创建对象-->
	<bean id="clientService" factory-bean="myFactoryBean" factory-method="get"/>

	<!--测试静态工厂方法创建对象-->
	<bean id="service" class="com.dmz.official.service.MyFactoryBean" factory-method="staticGet"/>
</beans>
```

断点如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235407882.jpg)

可以发现，这种情况也进入了`instantiateUsingFactoryMethod`方法中。通过静态工厂方法这种方式特殊之处在于，包含这个静态方法的类，不需要实例化，不需要被Spring管理。Spring的调用逻辑大概是：

1. 通过`<bean>`标签中的class属性得到一个Class对象
2. 通过Class对象获取到对应的方法名称的Method对象
3. 最后反射调用`Method.invoke(null,args)`

因为是静态方法，方法在执行时，不需要一个对象。

- 通过实例工厂方法的方式

测试代码（配置文件不变）：

```java
public static void main(String[] args) {
    ClassPathXmlApplicationContext cc = new ClassPathXmlApplicationContext("application.xml");
    System.out.println(cc.getBean("clientService"));
}
```

断点如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235417499.jpg)

还是执行的这个方法。这个方法的执行过程我断点跟踪了以后，发现跟`@Bean`方式执行的流程是一样的。这里也不再赘述了。

到这里，这段代码我们算结合官网大致过了一遍。其实还遗留了以下几个问题：

1. Spring是如何推断构造函数的？我们在上面验证的都是无参的构造函数，并且只提供了一个构造函数
2. Spring是如何推断方法的？不管是静态工厂方法，还是实例工厂方法的方式，我们都只在类中提供了一个跟配置匹配的方法名，假设我们对方法进行了重载呢？

要说清楚这两个问题需要比较深入的研究代码，同时进行测试。我们在官网学习过程中，暂时不去强求这类问题。这里提出来是为了在源码学习过程中，我们可以带一定目的性去阅读。

### 实例化总结：

1. 对象实例化，只是得到一个对象，还不是一个完全的Spring中的Bean，我们实例化后的这个对象还没有完成依赖注入，没有走完一系列的声明周期，这里需要大家注意

2. Spring官网上指明了，在Spring中实例化一个对象有三种方式：

   - 构造函数
   - 实例工厂方法
   - 静态工厂方法

3. 我自己总结如下结论：

   Spring通过解析我们的配置元数据，以及我们提供的类对象得到一个Beanfinition对象。通过这个对象可以实例化出一个java bean对象。主要流程如图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235508810.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

这篇文章到这里就结束了，主要学习了Spring官网中的1.2，1.3两小节。下篇文章，我们开始学习1.4中的知识。主要涉及到依赖注入的一些内容，也是我们Spring中非常重要的一块内容哦！下篇文章再见！

## Spring官网阅读（二）依赖注入及方法注入

### 依赖注入：

根据官网介绍，依赖注入主要分为两种方式

1. 构造函数注入
2. Setter方法注入

官网：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235737849.jpg)

我们分别对以上两种方式进行测试，官网上用的是XML的方式，我这边就采用注解的方式了：

测试代码如下，我们通过在Service中注入LuBanService这个过程来

```java
public class Main02 {
	public static void main(String[] args) {
    // config类主要完成对类的扫描
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(Config.class);
		Service service = (Service) ac.getBean("service");
		service.test();
	}
}
```

```java
@Component
public class LuBanService {
	LuBanService(){
		System.out.println("luBan create ");
	}
}
```

#### 测试setter方法注入

```java
@Component
public class Service {
	private LuBanService luBanService;

	public Service() {
		System.out.println("service create");
	}

	public void test(){
		System.out.println(luBanService);
	}
  
	// 通过autowired指定使用set方法完成注入
	@Autowired
	public void setLuBanService(LuBanService luBanService) {
		System.out.println("注入luBanService by setter");
		this.luBanService = luBanService;
	}
}
```

输出如下：

------

```java
luBan create 
service create
注入luBanService by setter  // 验证了确实是通过setter注入的
com.dmz.official.service.LuBanService@5a01ccaa
```

------

#### 测试构造函数注入

```java
@Component
public class Service {

	private LuBanService luBanService;
    
    public Service() {
		System.out.println("service create by no args constructor");
	}
	
    // 通过Autowired指定使用这个构造函数，否则默认会使用无参
	@Autowired
	public Service(LuBanService luBanService) {
		System.out.println("注入luBanService by constructor with arg");
		this.luBanService = luBanService;
		System.out.println("service create by constructor with arg");
	}

	public void test(){
		System.out.println(luBanService);
	}
}
```

------

输出如下：

```java
luBan create 
注入luBanService by constructor // 验证了确实是通过constructor注入的
service create by constructor
com.dmz.official.service.LuBanService@1b40d5f0
```

------

#### 疑问：

在上面的验证中，大家可能会有以下几个疑问：

1. `@Autowired`直接加到字段上跟加到set方法上有什么区别？为什么我们验证的时候需要将其添加到setter方法上？
   - 首先我们明确一点，直接添加`@Autowired`注解到字段上，不需要提供setter方法也能完成注入。以上面的例子来说，Spring会通过反射获取到Service中luBanService这个字段，然后通过反射包的方法，Filed.set(Service,luBanService)这种方式来完成注入
   - 我们将`@Autowired`添加到setter方法时，我们可以通过断点看一下方法的调用栈，如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235753215.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

对于这种方式来说，最终是通过Method.invoke(object,args)的方式来完成注入的，这里的method对象就是我们的setter方法

1. `@Autowired`为什么加到构造函数上可以指定使用这个构造函数？

   - 我们先可以测试下，如果我们不加这个注解会怎么样呢？我把前文中的`@Autowired`注解注释，然后运行发现

   ------

   ```java
   luBan create 
   service create by no args constructor  // 可以看到执行的是空参构造
   null
   ```
   
------
   
先不急得出结论，我们再进行一次测试，就是两个函数上都添加`@Autowired`注解呢？
   
```java
   Exception in thread "main" org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'service': Invalid autowire-marked constructor: public com.dmz.official.service.Service(com.dmz.official.service.LuBanService). Found constructor with 'required' Autowired annotation already: public com.dmz.official.service.Service()
   ```
   
   发现直接报错了，报错的大概意思是已经找到了一个被`@Autowired`注解标记的构造函数，同时这个注解中的required属性为true。后来我测试了将其中一个注解中的required属性改为false，发现还是报同样的错，最终将两个注解中的属性都改为false测试才通过，并且测试结果跟上面的一样，都是执行的无参构造。

   要说清楚这一点，涉及到两个知识

   - Spring中的注入模型，下篇文章专门讲这个
- Spring对构造函数的推断。这个到源码阶段我打算专门写一篇文章，现在我们暂且记得：
   
   > 在***默认的注入模型***下，Spring如果同时找到了两个***符合要求的构造函数***，那么Spring会采用默认的无参构造进行实例化，如果这个时候没有无参构造，那么此时会报错`java.lang.NoSuchMethodException`。什么叫符合要求的构造函数呢？就是构造函数中的参数Spring能找到，参数被Spring所管理。
>
   > 这里需要着重记得：**一，默认注入模型；二，符合要求的构造函数**
   
2. 如果我们同时采用*<u>构造注入</u>*加*<u>属性注入</u>*会怎么样呢？

   在没有进行测试前，我们可以大胆猜测下，Spring虽然能在构造函数里完成属性注入，但是这属于实例化对象阶段做的事情，那么在后面真正进行属性注入的时候，肯定会将其覆盖掉。现在我们来验证我们的结论

   ```java
   @Component
   public class Service {
     
   	private LuBanService luBanService;	
     
     // 这里是构造注入
   	public Service(LuBanService luBanService) {
   		System.out.println("注入luBanService by constructor with arg");
   		this.luBanService = luBanService;
   		System.out.println("service create by constructor with arg");
   	}
     
   	public void test(){
   		System.out.println(luBanService);
   	}
     
     // 这里是属性注入
   	@Autowired
   	public void setLuBanService(LuBanService luBanService) {
		System.out.println("注入luBanService by setter");
   		this.luBanService = null;
	}
   }

   ```
   
   运行结果：
   
   ------
   
   ```java
注入luBanService by constructor with arg  // 实例化时进行了一次注入
   service create by constructor with arg   // 完成了实例化
   注入luBanService by setter                // 属性注入时将实例化时注入的属性进行了覆盖
   null
   ```
   
   ------

#### 区别：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235808677.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

根据上图中官网所说，我们可以得出如下结论：

1. 构造函数注入跟setter方法注入可以混用
2. 对于一些强制的依赖，我们最好使用构造函数注入，对于一些可选依赖我们可以采用setter方法注入
3. Spring团队推荐使用构造函数的方式完成注入。但是对于一些参数过长的构造函数，Spring是不推荐的

### 方法注入：

我们不完全按照官网顺序进行学习，先看这一小节，对应官网上的位置如下图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235822739.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

#### 为什么需要方法注入：

首先我们思考一个问题，在有了依赖注入的情况下，为什么还需要方法注入这种方式呢？换而言之，方法注入解决了什么问题？

我们来看下面这种场景：

```java
@Component
public class MyService {

	@Autowired
	private LuBanService luBanService;

	public void test(int a){
		luBanService.addAndPrint(a);
	}
}
```

```java
@Component
// 原型对象
@Scope("prototype")
public class LuBanService {
	int i;

	LuBanService() {
		System.out.println("luBan create ");
	}
	// 每次将当前对象的属性i+a然后打印
	public void addAndPrint(int a) {
		i+=a;
		System.out.println(i);
	}
}
```

```java
public class Main02 {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(Config.class);
		MyService service = (MyService) ac.getBean("myService");
		service.test(1);
		service.test(2);
		service.test(3);
	}
}
```

在上面的代码中，我们有两个Bean：MyService为单例的Bean，LuBanService为原型的Bean。我们的本意可能是希望每次都能获取到不同的LuBanService，预期的结果应该打印出：

------

1，2，3

------

实际输出：

------

1
3
6

------

这个结果说明我们每次调用到的LuBanService是同一个对象。当然，这也很好理解，因为在依赖注入阶段我们就完成了LuBanService的注入，之后我们在调用测试方法时，不会再去进行注入，所以我们一直使用的是同一个对象。

我们可以这么说，原型对象在这种情况下，失去了原型的意义，因为每次都使用的是同一个对象。那么如何解决这个问题呢？只要我每次在使用这个Bean的时候都去重新获取就可以了，那么这个时候我们可以通过方法注入来解决。

#### 一、通过注入上下文（applicationContext对象）

又分为以下两种方式：

- 实现`org.springframework.context.ApplicationContextAware`接口

```java
@Component
public class MyService implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public void test(int a) {
		LuBanService luBanService = ((LuBanService) applicationContext.getBean("luBanService"));
		luBanService.addAndPrint(a);
	}

	@Override
	public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
```

- 直接注入上下文

```java
@Component
public class MyService{
  
	@Autowired
	private ApplicationContext applicationContext;

	public void test(int a) {
		LuBanService luBanService = ((LuBanService) applicationContext.getBean("luBanService"));
		luBanService.addAndPrint(a);
	}
}
```

#### 二、通过@LookUp的方式

（也分为注解跟XML两种方式，这里只演示注解的）

```java
@Component
public class MyService{
	public void test(int a) {
		LuBanService luBanService = lookUp();
		luBanService.addAndPrint(a);
	}
	// 
	@Lookup
	public LuBanService lookUp(){
		return null;
	}
}
```

#### 三、方法注入之 replace-method

方法注入还有一种方式，即通过`replace-method`这种形式，没有找到对应的注解，所以这里我们也就用XML的方式测试一下：

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
	<bean id="myService" class="com.dmz.official.service.MyService">
		<replaced-method replacer="replacer" name="test"/>
	</bean>

	<bean id="replacer" class="com.dmz.official.service.MyReplacer"/>
</beans>
~~~

~~~java
public class MyReplacer implements MethodReplacer {
   @Override
   public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
        System.out.println("替代"+obj+"中的方法，方法名称："+method.getName());
        System.out.println("执行新方法中的逻辑");
        return null;
    }
}

public class MyService{
    public void test(int a) {
        System.out.println(a);
    }
}

public class Main {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext cc = new ClassPathXmlApplicationContext("application.xml");
        MyService myService = ((MyService) cc.getBean("myService"));
        myService.test(1);
    }
}
~~~

------

执行结果：

```
替代com.dmz.official.service.MyService$$EnhancerBySpringCGLIB$$61c14242@63e31ee中的方法，方法名称：test
执行新方法中的逻辑
12
```

------

**这里需要注意一点：**

我在测试replace-method这种方法注入的方式时，受动态代理的影响，一直想将执行我们被替代的方法。用代码体现如下：

```java
public class MyReplacer implements MethodReplacer {

	@Override
	public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
//		System.out.println("替代"+obj+"中的方法，方法名称："+method.getName());
//		System.out.println("执行新方法中的逻辑");
		method.invoke(obj,args);
		return null;
	}
}
```

但是，这段代码是无法执行的，会报栈内存溢出。因为obj是我们的代理对象，`method.invoke(obj,args)`执行时会进入方法调用的死循环。最终我也没有找到一种合适的方式来执行被替代的方法。目前看来这可能也是Spring的设计，所以我们使用replace-method的场景应该是想完全替代某种方法的执行逻辑，而不是像AOP那样更多的用于在方法的执行前后等时机完成某些逻辑。

### 依赖注入跟方法注入的总结：

- 我们首先要明确一点，什么是依赖（Dependencies）？来看官网中的一段话：

> ![在这里插入图片描述](https://img-blog.csdnimg.cn/20191217235843318.jpg)
>
> 可以说，一个对象的依赖就是它自身的属性，Spring中的**依赖注入就是属性注入**。

- 我们知道一个对象由两部分组成：属性+行为（方法），可以说Spring通过属性注入+方法注入的方式掌控的整个bean。
- 属性注入跟方法注入都是Spring提供给我们用来处理Bean之间协作关系的手段
- 属性注入有两种方式：构造函数，Setter方法。
- 方法注入（LookUp Method跟Replace Method）需要依赖动态代理完成
- 方法注入对属性注入进行了一定程度上的补充，因为属性注入的情况下，原型对象可能会失去原型的意义

画图如下：

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70.jpeg)

## Spring官网阅读（三）自动注入

### 前言

在看下面的内容之前，我们先要对自动注入及精确注入有一个大概的了解，所谓**精确注入**就是指，我们通过构造函数或者setter方法指定了我们对象之间的依赖，也就是我们上篇文章中讲到的**依赖注入**，然后Spring根据我们指定的依赖关系，精确的给我们完成了注入。那么**自动注入**是什么？我们看下面一段代码：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	     xsi:schemaLocation="http://www.springframework.org/schema/beans   http://www.springframework.org/schema/dbeans/spring-beans.xsd">
  
	<bean id="auto" class="com.dmz.official.service.AutoService" autowire="byType"/>
	<bean id="dmzService" class="com.dmz.official.service.DmzService"/>
</beans>
```

~~~java
public class AutoService {
  
	DmzService service;
	
  public void setService(DmzService dmzService){
		System.out.println("注入dmzService"+dmzService);
		service = dmzService;
	}
}
~~~

```java
public class DmzService {
}
```

```java
public class Main03 {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext cc = new ClassPathXmlApplicationContext("application.xml");
		System.out.println(cc.getBean("auto"));
	}
}
```

在上面的例子中我们可以看到

1. 我们没有采用注解`@Autowired`进行注入
2. XML中没有指定属性标签`<property>`
3. 没有使用构造函数

但是，打印结果如下：

------

```java
注入dmzServicecom.dmz.official.service.DmzService@73a8dfcc  // 这里完成了注入
com.dmz.official.service.AutoService@1963006a
```

------

可能细心的同学已经发现了，在`AutoService`的标签中我们新增了一个属性`autowire="byType"`,那么这个属性是什么意思呢？为什么加这个属性就能帮我们完成注入呢？不要急，我们带着问题继续往下看。![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218000049457.png)

### 什么是自动注入：

这部分内容主要涉及官网中的[1.4.5](https://docs.spring.io/spring/docs/5.2.1.RELEASE/spring-framework-reference/core.html#beans-autowired-exceptions)小结。

我们先看官网上怎么说的：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218000334884.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

### 自动注入的优点：

大概翻译如下：

Spring可以自动注入互相协作的bean之间的依赖。自动注入有以下两个好处：

- 自动注入能显著的减少我们指定属性或构造参数的必要。这个不难理解，我们在上篇文章中讲过了，依赖注入的两种方式，setter方法跟构造函数，见上篇文章[依赖注入](https://daimingzhi.blog.csdn.net/article/Spring官网阅读（二）依赖注入及方法注入/Spring官网阅读（二）依赖注入及方法注入.md#dep)。在前言中的例子我们也能发现，我们并不需要指定属性或构造参数
- 自动装配可以随着对象的演化更新配置。例如，如果需要向类添加依赖项，则可以自动满足该依赖项，而不需要修改配置。因此，自动装配在**开发过程**中特别有用，但是当我们的代码库变的稳定时，自动装配也不会影响我们将装配方式切换到**精确注入**（这个词是我根据官网阅读加自己理解翻译过来的，也就是官网中的（**explicit wiring**）

### 注入模型：

接下来，官网给我们介绍了自动注入的四种模型，如图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218000107216.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

我们一一进行解析并测试：

- `no`

这是目前Spring默认的注入模型，也可以说默认情况下Spring是关闭自动注入，必须要我们通过setter方法或者构造函数完成依赖注入，并且Spring也不推荐修改默认配置。我们使用IDEA时也可以看到

[外链图片转存失败,源站可能有防盗链机制,建议将图片保存下来直接上传(img-DZC5fi2F-1576598384639)(image/2019120204.jpg)]

用红线框出来的部分建议我们使用精确的方式注入依赖。

从上面来说，Spring自动注入这种方式在我们实际开发中基本上用不到，但是为了更好的理解跟学习Spring源码，我们也是需要好好学习这部分知识的。

- `byName`

这种方式，我们为了让Spring完成自动注入需要提供两个条件

1. 提供setter方法
2. 如果需要注入的属性为`xxx`,那么setter方法命名必须是`setXxx`,也就是说，命名必须规范

在找不到对应名称的bean的情况下，Spring也不会报错，只是不会给我们完成注入。

测试代码：

```java
//记得需要将配置信息修改为：<bean id="auto" class="com.dmz.official.service.AutoService" autowire="byName"/>

public class AutoService {
	
  DmzService dmzService;
  
	/**
	 * 	setXXX,Spring会根据XXX到容器中找对应名称的bean,找到了就完成注入
 	 */
	public void setDmzService(DmzService dmzService){
		System.out.println("注入dmzService"+dmzService);
		service = dmzService;
	}
}
```

另外我在测试的时候发现，这种情况下，如果我们提供的参数不规范也不会完成注入的，如下：

```java
public class AutoService {

	DmzService dmzService;
	
    // indexService也被Spring所管理
	IndexService indexService;

	/**
	 * setXXX,Spring会根据XXX到容器中找对应名称的bean,找到了就完成注入
	 * （本方法因为参数有两个，不会调用）
	 */
	public void setDmzService(DmzService dmzService, IndexService indexService) {
		System.out.println("注入dmzService" + dmzService);
		this.dmzService = dmzService;
	}
}
```

本以为这种情况Spring会注入dmzService，indexService为null，实际测试过程中发现这个set方法根本不会被调用，说明Spring在选择方法时，还对参数进行了校验，`byName`这种注入模型下，**参数只能是我们待注入的类型且只能有一个**

- `byType`

测试代码跟之前唯一不同的就是修改配置`autowire="byType"`,这里我们测试以下三种异常情况

1. 找不到合适类型的bean，发现不报异常，同时不进行注入
2. 找到了多个合适类型的bean，Spring会直接报错`Caused by: org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'com.dmz.official.service.DmzService' available: expected single matching bean but found 2: dmzService,dmzService2`
3. set方法中有两个参数，切两个参数都能找到唯一一个类型符合的bean，不报异常，也不进行注入

另外需要说明的是，我在测试的过程，将set方法仅仅命名为`set`,像这样`public void set(DmzService dmzService)`,这种情况下Spring也不会进行注入

**我们可以发现，对于这两种注入模型都是依赖setter方法完成注入的，并且对setter方法命名有一定要求（只要我们平常遵从代码书写规范，一般也不会踩到这些坑）。第一，不能有多个参数；第二，不能仅仅命名为`set`**

- `constructor`

当我们使用这种注入模型时，Spring会根据构造函数查找有没有对应参数名称的bean,有的话完成注入（跟前文的`byName`差不多），如果根据名称没找到，那么它会再根据类型进行查找，如果根据类型还是没找到，就会报错。

### 自动注入的缺陷：

这里不得不说一句，Spring官网在这一章节有三分之二的内容是在说自定注入的缺陷以及如何将一个类从自动注入中排除，结合默认情况下自动注入是关闭的（默认注入模型为`no`），可以说明，在实际使用情况中，Spring是非常不推荐我们开启自动注入这种模型的。从官网中我们总结自动注入有以下几个缺陷：

- 精确注入会覆盖自动注入。并且我们不能注入基本数据类型，字符串，Class类型（这些数据的数组也不行）。而且这是Spring故意这样设计的
- 自动注入不如精确注入准确。而且我们在使用自动注入时，对象之间的依赖关系不明确
- 对于一些为Spring容器生成文档的工具，无法获取依赖关系
- 容器中的多个bean定义可能会与自动注入的setter方法或构造函数参数指定的类型匹配。对于数组、集合或映射实例，这可能不会产生什么问题。但是，对于期望单个值的依赖项，我们无法随意确定到底有谁进行注入。如果没有唯一的bean定义可用，则会抛出异常

### 如何将Bean从自动注入中排除？

这里主要用到`autowire-candidate`这个属性，我们要将其设置为`false`，这里需要注意以下几点：

1. 这个设置只对类型注入生效。这也很好理解，例如我们告诉Spring要自动注入一个`indexService`,同时我们又在`indexService`的配置中将其从自动注入中排除，这就是自相矛盾的。所以在`byName`的注入模型下，Spring直接忽略了`autowire-candidate`这个属性
2. `autowire-candidate=false`这个属性代表的是，这个bean不作为候选bean注入到别的bean中，而不是说这个bean不能接受别的bean的注入。例如在我们上面的例子中我们对`AutoService`进行了如下配置：

```xml
<bean id="auto" class="com.dmz.official.service.AutoService" autowire="byType" autowire-candidate="false"/>
```

代表的是这个bean不会被注入到别的bean中，但是`dmzService`任何会被注入到`AutoService`中

另外需要说明的是，对于自动注入，一般我们直接在顶级的标签中进行全局设置，如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd"
 <!--在这里进行配置-->
 default-autowire="byName">
```

### 自动注入跟精确注入的比较总结：

连同上篇文章[依赖注入](https://daimingzhi.blog.csdn.net/article/Spring官网阅读（二）依赖注入及方法注入/Spring官网阅读（二）依赖注入及方法注入.md#dep)，我画了下面一个图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218000135996.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

- 从关注的点上来看，**自动注入是针对的整个对象**，或者一整批对象。比如我们如果将`autoService`这个bean的注入模型设置为`byName`，Spring会为我们去寻找所有符合要求的名字（通过set方法）bean并注入到`autoService`中。而**精确注入这种方式，是我们针对对象中的某个属性**，比如我们在`autoService`中的`dmzService`这个属性字段上添加了`@AutoWired`注解，代表我们要精确的注入`dmzService`这个属性。而**方法注入主要是基于方法对对象进行注入**。
- 我们通常所说***byName***,***byType\**\*跟我们在前文提到的注入模型中的`byName`,`byType`是完全不一样的。通常我们说的\**\*byName***,***byType***是Spring寻找bean的手段。比如，当我们注入模型为`constructor`时，Spring会先通过名称找对符合要求的bean，这种通过名称寻找对应的bean的方式我们可以称为`byName`。我们可以将一次注入分为两个阶段，首先是寻找符合要求的bean，其次再是将符合要求的bean注入。也可以画图如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218000218293.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

### 补充（1.4小结的剩余部分）

这部分比较简单，也是`1.4`小节中剩余的两个小知识，在这篇文章我们也一并学习了~

#### depends-on：

我们首先要知道，默认情况下，Spring在实例化容器中的对象时是按名称进行自然排序进行实例化的。比如我们现在有A,B,C三个对象，那么Spring在实例化时会按照A,B,C这样的顺序进行实例化。但是在某些情况下我们可能需要让B在A之前完成实例化，这个时候我们就需要使用`depends-on`这个属性了。我们可以通过形如下面的配置完成：

```xml
<bean id="a" class="xx.xx.A" depends-on="b"/>
<bean id="b" class="xx.xx.B" />
```

或者：

```java
@Component
@DependsOn("b")
public class A {
}
```

#### lazy:

默认情况下，Spring会在容器启动阶段完成所有bean的实例化，以及一系列的生命周期回调。某些情况下，我们

可能需要让某一个bean延迟实例化。这种情况下，我们需要用到`lazy`属性，有以下两种方式：

1. XML中bean标签的`lazy-init`属性

```xml
<bean id="lazy" class="com.something.ExpensiveToCreateBean" lazy-init="true"/>
```

1. `@Lazy`注解

```java
@Component
// 懒加载
@Lazy
public class A {
	
}
```

到此为止，官网中`1.4`小节中的内容我们就全学习完啦！最核心的部分应该就是上文中的这个[图](https://daimingzhi.blog.csdn.net/article/details/103589903#jump)了。我们主要总结了Spring让对象产生依赖的方式，同时对各个方式进行了对比。通过这部分的学习，我觉得大家应该对Spring的依赖相关知识会更加系统，这样我们之后学习源码时碰到疑惑也会少很多。

下面我们还要继续学习Spring的官网，比如前面文章提到的`Beandefinition`到底是什么东西？Spring中的Bean的生命周期回调又是什么？这些在官网中都能找到答案。

## Spring官网阅读（四）BeanDefinition（上）

### BeanDefinition是什么？

> 我们先看官网上是怎么解释的：
>
> ![在这里插入图片描述](https://img-blog.csdnimg.cn/2019121800044452.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

从上文中，我们可以得出以下几点结论：

1. `BeanDefinition`包含了我们对bean做的配置，比如XML`<bean/>`标签的形式进行的配置
2. 换而言之，Spring将我们对bean的定义信息进行了抽象，抽象后的实体就是`BeanDefinition`,**并且Spring会以此作为标准来对Bean进行创建**
3. `BeanDefinition`包含以下元数据：
   - 一个全限定类名，通常来说，就是对应的bean的全限定类名。
   - bean的行为配置元素，这些元素展示了这个bean在容器中是如何工作的包括`scope`(域，我们文末有简单介绍)，`lifecycle callbacks`(生命周期回调，下篇文章介绍)等等
   - 这个bean的依赖信息
   - 一些其他配置信息，比如我们配置了一个连接池对象，那么我们还会配置它的池子大小，最大连接数等等

在这里，我们来比较下，正常的创建一个bean，跟Spring通过抽象出一个`BeanDefinition`来创建bean有什么区别：

**正常的创建一个java bean:**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218000454639.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

**Spring通过`BeanDefinition`来创建bean:**

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019121800050357.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

通过上面的比较，我们可以发现，相比于正常的对象的创建过程，Spring对其管理的bean没有直接采用new的方式，而是先通过解析配置数据以及根据对象本身的一些定义而获取其对应的`beandefinition`,并将这个`beandefinition`作为之后创建这个bean的依据。同时Spring在这个过程中提供了一些扩展点，例如我们在图中所提到了`BeanfactoryProcessor`。这些大家先作为了解，之后在源码阶段我们再分析。

### BeanDefinition的方法分析

这里对于每个字段我只保留了一个方法，只要知道了字段的含义，方法的含义我们自然就知道了

```java
// 获取父BeanDefinition,主要用于合并，下节中会详细分析
String getParentName();

// 对于的bean的ClassName
void setBeanClassName(@Nullable String beanClassName);

// Bean的作用域，不考虑web容器，主要两种，单例/原型，见官网中1.5内容
void setScope(@Nullable String scope);

// 是否进行懒加载
void setLazyInit(boolean lazyInit);

// 是否需要等待指定的bean创建完之后再创建
void setDependsOn(@Nullable String... dependsOn);

// 是否作为自动注入的候选对象
void setAutowireCandidate(boolean autowireCandidate);

// 是否作为主选的bean
void setPrimary(boolean primary);

// 创建这个bean的类的名称
void setFactoryBeanName(@Nullable String factoryBeanName);

// 创建这个bean的方法的名称
void setFactoryMethodName(@Nullable String factoryMethodName);

// 构造函数的参数
ConstructorArgumentValues getConstructorArgumentValues();

// setter方法的参数
MutablePropertyValues getPropertyValues();

// 生命周期回调方法，在bean完成属性注入后调用
void setInitMethodName(@Nullable String initMethodName);

// 生命周期回调方法，在bean被销毁时调用
void setDestroyMethodName(@Nullable String destroyMethodName);

// Spring可以对bd设置不同的角色,了解即可，不重要
// 用户定义 int ROLE_APPLICATION = 0;
// 某些复杂的配置    int ROLE_SUPPORT = 1;
// 完全内部使用   int ROLE_INFRASTRUCTURE = 2;
void setRole(int role);

// bean的描述，没有什么实际含义
void setDescription(@Nullable String description);

// 根据scope判断是否是单例
boolean isSingleton();

// 根据scope判断是否是原型
boolean isPrototype();

// 跟合并beanDefinition相关，如果是abstract，说明会被作为一个父beanDefinition，不用提供class属性
boolean isAbstract();

// bean的源描述，没有什么实际含义 
String getResourceDescription();

// cglib代理前的BeanDefinition
BeanDefinition getOriginatingBeanDefinition();
```

### BeanDefinition的继承关系

类图如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218000510989.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

#### 1.AttributeAccessor

- `org.springframework.core.AttributeAccessor`

先来看接口上标注的这段`java doc`

> Interface defining a generic contract for attaching and accessing metadata to/from arbitrary objects.

翻译下来就是：

这个接口为从其它任意类中获取或设置元数据提供了一个通用的规范。

其实这就是`访问者`模式的一种体现，采用这方方法，我们可以将**数据接口**跟**操作方法**进行分离。

我们再来看这个接口中定义的方法：

```java
void setAttribute(String name, @Nullable Object value);

Object getAttribute(String name);

Object removeAttribute(String name);

boolean hasAttribute(String name);

String[] attributeNames();
```

就是提供了一个获取属性跟设置属性的方法

那么现在问题来了，在我们整个`BeanDefiniton`体系中，这个被操作的**数据结构**在哪呢？不要急，在后文中的`AbstractBeanDefinition`会介绍。

- `org.springframework.beans.BeanMetadataElement`

我们还是先看`java doc`:

> Interface to be implemented by bean `metadata` elements that carry a configuration source object.

翻译：这个接口提供了一个方法去获取配置源对象，其实就是我们的原文件。

这个接口只提供了一个方法：

```java
@Nullable
Object getSource();
12
```

我们可以理解为，当我们通过注解的方式定义了一个`IndexService`时，那么此时的`IndexService`对应的`BeanDefinition`通过`getSource`方法返回的就是`IndexService.class`这个文件对应的一个`File`对象。

如果我们通过`@Bean`方式定义了一个`IndexService`的话，那么此时的source是被`@Bean`注解所标注的一个`Mehthod`对象。

#### 2.AbstractBeanDefinition

##### AbstractBeanDefinition的继承关系

先看一下类图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019121800053340.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

- `org.springframework.core.AttributeAccessorSupport`

可以看到这个类实现了`AttributeAccerror`接口，我们在上文中已经提到过，`AttributeAccerror`采用了[**访问者**](https://daimingzhi.blog.csdn.net/article/details/103589939#jump)的涉及模式，将**数据结构**跟**操作方法**进行了分离，数据结构在哪呢？就在`AttributeAccessorSupport`这个类中，我们看下它的代码：

```java
public abstract class AttributeAccessorSupport implements AttributeAccessor, Serializable {
	/** Map with String keys and Object values. */
	private final Map<String, Object> attributes = new LinkedHashMap<>();

    @Override
	public void setAttribute(String name, @Nullable Object value) {
		Assert.notNull(name, "Name must not be null");
		if (value != null) {
			this.attributes.put(name, value);
		}
		else {
			removeAttribute(name);
		}
	}
	......省略下面的代码
123456789101112131415
```

可以看到，在这个类中，维护了一个map，这就是`BeanDefinition`体系中，通过`访问者模式`所有操作的数据对象。

- `org.springframework.beans.BeanMetadataAttributeAccessor`

这个类主要就是对我们上面的map中的数据操作做了更深一层的封装，我们就看其中的两个方法：

```java
public void addMetadataAttribute(BeanMetadataAttribute attribute) {
    super.setAttribute(attribute.getName(), attribute);
}
public BeanMetadataAttribute getMetadataAttribute(String name) {
    return (BeanMetadataAttribute) super.getAttribute(name);
}
123456
```

可以发现，它只是将属性统一封装成了一个`BeanMetadataAttribute`,然后就调用了父类的方法，将其放入到map中。

我们的`AbstractBeanDefinition`通过继承了`BeanMetadataAttributeAccessor`这个类，可以对`BeanDefinition`中的属性进行操作。这里说的属性仅仅指的是`BeanDefinition`中的一个map，而不是它的其它字段。

##### 为什么需要AbstractBeanDefinition？

对比`BeanDefinition`的源码我们可以发现，`AbstractBeanDefinition`对`BeanDefinition`的大部分方法做了实现（没有实现`parentName`相关方法）。同时定义了一系列的常量及默认字段。这是因为`BeanDefinition`接口过于顶层，如果我们依赖`BeanDefinition`这个接口直接去创建其实现类的话过于麻烦，所以通过`AbstractBeanDefinition`做了一个下沉，并给很多属性赋了默认值，例如：

```java
// 默认情况不是懒加载的
private boolean lazyInit = false;
// 默认情况不采用自动注入
private int autowireMode = AUTOWIRE_NO;
// 默认情况作为自动注入的候选bean
private boolean autowireCandidate = true;
// 默认情况不作为优先使用的bean
private boolean primary = false;
........
123456789
```

这样可以方便我们创建其子类，如我们接下来要讲的：`ChildBeanDefinition`,`RootBeanDefinition`等等

#### 3.AbstractBeanDefinition的三个子类

##### GenericBeanDefinition

- 替代了原来的`ChildBeanDefinition`，比起`ChildBeanDefinition`更为灵活，`ChildBeanDefinition`在实例化的时候必须要指定一个`parentName`,而`GenericBeanDefinition`不需要。我们通过注解配置的bean以及我们的配置类（除`@Bena`外）的`BeanDefiniton`类型都是`GenericBeanDefinition`。

##### ChildBeanDefinition

- 现在已经被`GenericBeanDefinition`所替代了。我在`5.1.x`版本没有找到使用这个类的代码。

##### RootBeanDefinition

- Spring在启动时会实例化几个初始化的`BeanDefinition`,这几个`BeanDefinition`的类型都为`RootBeanDefinition`
- Spring在合并`BeanDefinition`返回的都是`RootBeanDefinition`
- 我们通过`@Bean`注解配置的bean，解析出来的`BeanDefinition`都是`RootBeanDefinition`（实际上是其子类`ConfigurationClassBeanDefinition`）

#### 4.AnnotatedBeanDefinition

这个接口继承了我们的`BeanDefinition`接口，我们查看其源码可以发现：

```java
AnnotationMetadata getMetadata();

@Nullable
MethodMetadata getFactoryMethodMetadata();
1234
```

这个接口相比于`BeanDefinition`， 仅仅多提供了两个方法

- ```
  getMetadata()
  ```

  ,主要用于获取注解元素据。从接口的命名上我们也能看出，这类主要用于保存通过注解方式定义的bean所对应的

  ```
  BeanDefinition
  ```

  。所以它多提供了一个关于获取注解信息的方法

   

  - `getFactoryMethodMetadata()`,这个方法跟我们的`@Bean`注解相关。当我们在一个配置类中使用了`@Bean`注解时，被`@Bean`注解标记的方法，就被解析成了`FactoryMethodMetadata`。

#### 5.AnnotatedBeanDefinition的三个实现类

##### AnnotatedGenericBeanDefinition

- 通过形如下面的API注册的bean都是`AnnotatedGenericBeanDefinition`

```java
public static void main(String[] args) {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
    ac.register(Config.class);
}
1234
```

这里的`config`对象，最后在Spring容器中就是一个`AnnotatedGenericBeanDefinition`。

- 通过`@Import`注解导入的类，最后都是解析为`AnnotatedGenericBeanDefinition`。

##### ScannedGenericBeanDefinition

- 都过注解扫描的类，如`@Service`,`@Compent`等方式配置的Bean都是`ScannedGenericBeanDefinition`

##### ConfigurationClassBeanDefinition

- 通过`@Bean`的方式配置的Bean为`ConfigurationClassBeanDefinition`

最后，我们还剩一个`ClassDerivedBeanDefinition`,这个类是跟`kotlin`相关的类，一般用不到，笔者也不熟，这里就不管了！

### 总结

至此，我们算完成了`BeanDefinition`部分的学习，在下一节中，我将继续跟大家一起学习`BeanDefinition`合并的相关知识。这篇文章中，主要学习了

1. 什么是`BeanDefinition`，总结起来就是一句话，Spring创建bean时的建模对象。
2. `BeanDefinition`的具体使用的子类，以及Spring在哪些地方使用到了它们。这部分内容在后面的学习中很重要，画图总结如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218000556676.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

### 1.5小结内容的补充

#### 单例

一个单例的bean意味着，这个bean只会容器创建一次。在创建后，容器中的每个地方使用的都是同一个bean对象。这里用Spring官网上的一个原图：
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019121800062047.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)
在上面图片的例子中，`accountDao`在被其它三个bean引用，这三个引用指向的都是同一个bean。

在默认情况下，Spring中bean的默认域就是单例的。分XML跟注解两种配置方式：

```xml
<!--即使配置singleton也是单例的，这是Spring的默认配置-->
<bean id="accountService" class="com.something.DefaultAccountService" scope="singleton"/>
```

~~~java
@Component
// 这里配置singleton，默认就是singleton
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LuBanService{
}
~~~

#### 原型

一个原型的bean意味着，每次我们使用时都会重新创建这个bean。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218000626409.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

在上面图片的例子中，`accountDao`在被其它三个bean引用，这三个引用指向的都是一个新建的bean。

两种配置方式：

```xml
<bean id="accountService" class="com.something.DefaultAccountService" scope="prototype"/>
```

~~~java
@Component
// 这里配置prototype
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LuBanService{
}
~~~



## Spring官网阅读（五）BeanDefinition（下）

在 [上篇文章](https://daimingzhi.blog.csdn.net/article/Spring官网阅读（四）BeanDefinition（上）/Spring官网阅读（四）BeanDefinition.md)中，我们学习了 `BeanDefinition`的一些属性，其中有以下几个属性：

```java
//  是否抽象
boolean isAbstract();
// 获取父BeanDefinition的名称
String getParentName();
1234
```

上篇文章中说过，这几个属性跟`BeanDefinition`的合并相关，那么我先考虑一个问题，什么是合并呢？

#### 什么是合并？

> 我们来看官网上的一段介绍：
> ![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107002020136.jpg)
> 大概翻译如下：

一个`BeanDefinition`包含了很多的配置信息，包括构造参数，setter方法的参数还有容器特定的一些配置信息，比如初始化方法，静态工厂方法等等。一个子`BeanDefinition`可以从它的父`BeanDefinition`继承配置信息，不仅如此，还可以覆盖其中的一些值或者添加一些自己需要的属性。使用`BeanDefinition`的父子定义可以减少很多的重复属性的设置，父`BeanDefinition`可以作为`BeanDefinition`定义的模板。

我们通过一个例子来观察下合并发生了什么，编写一个Demo如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
	<bean id="parent" abstract="true" class="com.dmz.official.merge.TestBean">
		<property name="name" value="parent"/>
		<property name="age" value="1"/>
	</bean>
	<bean id="child" class="com.dmz.official.merge.DerivedTestBean" parent="parent" > <!-- 注意parent属性 -->
		<property name="name" value="override"/>
	</bean>
</beans>
```

~~~java
public class DerivedTestBean {
	private String name;

	private int age;
    // 省略getter setter方法
}

public class TestBean {
	private String name;
	private String age;
  // 省略getter setter方法
}

public class Main {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext cc = new ClassPathXmlApplicationContext("application.xml");
		DerivedTestBean derivedTestBean = (DerivedTestBean) cc.getBean("child");
		System.out.println("derivedTestBean的name = " + derivedTestBean.getName());
		System.out.println("derivedTestBean的age = " + derivedTestBean.getAge());
	}
}
~~~

运行：

------

```
derivedTestBean的name = override
derivedTestBean的age = 1
```

------

在上面的例子中，**我们将`DerivedTestBean`的`parent`属性设置为了`parent`,指向了我们的`TestBean`，同时将`TestBean`的age属性设置为1**，但是我们在配置文件中并没有直接设置`DerivedTestBean`的age属性。但是在最后运行结果，我们可以发现，`DerivedTestBean`中的age属性已经有了值，并且为1，就是我们在其parent Bean（也就是`TestBean`）中设置的值。也就是说，**子`BeanDefinition`会从父`BeanDefinition`中继承没有的属性**。另外，`DerivedTestBean`跟`TestBean`都指定了name属性，但是可以发现，这个值并没有被覆盖掉，也就是说，**子`BeanDefinition`中已经存在的属性不会被父`BeanDefinition`中所覆盖**。

##### 合并的总结：

所以我们可以总结如下：

- **子`BeanDefinition`会从父`BeanDefinition`中继承没有的属性**
- 这个过程中，**子`BeanDefinition`中已经存在的属性不会被父`BeanDefinition`中所覆盖**

##### 关于合并需要注意的点：

另外我们需要注意的是：

- 子`BeanDefinition`中的`class`属性如果为null，同时父`BeanDefinition`又指定了`class`属性，那么子`BeanDefinition`也会继承这个`class`属性。
- 子`BeanDefinition`必须要兼容父`BeanDefinition`中的所有属性。这是什么意思呢？以我们上面的demo为例，我们在父`BeanDefinition`中指定了name跟age属性，但是如果子`BeanDefinition`中子提供了一个name的setter方法，这个时候Spring在启动的时候会报错。因为子`BeanDefinition`不能承接所有来自父`BeanDefinition`的属性
- 关于`BeanDefinition`中`abstract`属性的说明：
  1. 并不是作为父`BeanDefinition`就一定要设置`abstract`属性为true，`abstract`只代表了这个`BeanDefinition`是否要被Spring进行实例化并被创建对应的Bean，如果为true，代表容器不需要去对其进行实例化。
  2. 如果一个`BeanDefinition`被当作父`BeanDefinition`使用，并且没有指定其`class`属性。那么必须要设置其`abstract`为true
  3. `abstract=true`一般会跟父`BeanDefinition`一起使用，因为当我们设置某个`BeanDefinition`的`abstract=true`时，一般都是要将其当作`BeanDefinition`的模板使用，否则这个`BeanDefinition`也没有意义，除非我们使用其它`BeanDefinition`来继承它的属性

#### Spring在哪些阶段做了合并？

> **下文将所有`BeanDefinition`简称为`bd`**

##### 1、扫描并获取到`bd`：

这个阶段的操作主要发生在`invokeBeanFactoryPostProcessors`，对应方法的调用栈如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107002153661.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

对应的执行该方法的类为：`PostProcessorRegistrationDelegate`

方法源码如下：

```java
	public static void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory,
													   List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
      // .....
	    // 省略部分代码，省略的代码主要时用来执行程序员手动调用API注册的容器的后置处理器
      // .....

		  // 发生一次bd的合并
      // 这里只会获取实现了BeanDefinitionRegistryPostProcessor接口的Bean的名字
			String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
         // 筛选实现了PriorityOrdered接口的后置处理器
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					// 去重
					processedBeans.add(ppName);
				}
			}
			// .....
      // 只存在一个internalConfigurationAnnotationProcessor 处理器，用于扫描
      // 这里只会执行了实现了PriorityOrdered跟BeanDefinitionRegistryPostProcessor的后置处理器
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			// .....
      // 这里又进行了一个bd的合并
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
        // 筛选实现了Ordered接口的后置处理器
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			// .....
      // 执行的是实现了BeanDefinitionRegistryPostProcessor接口跟Ordered接口的后置处理器
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
        // 这里再次进行了一次bd的合并
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
            // 筛选只实现了BeanDefinitionRegistryPostProcessor的后置处理器
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
        // 执行的是普通的后置处理器，即没有实现任何排序接口（PriorityOrdered或Ordered)
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}
        // .....
        // 省略部分代码，这部分代码跟BeanfactoryPostProcessor接口相关，这节bd的合并无关，下节容器的扩展点中我会介绍
        // .....
		
	}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107002432629.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)
大家可以结合我画的图跟上面的代码过一遍流程，只要弄清楚一点就行，即每次调用`beanFactory.getBeanNamesForType`都进行了一次`bd`的合并。`getBeanNamesForType`这个方法主要目的是为了或者指定类型的`bd`的名称，之后通过`bd`的名称去找到指定的`bd`，然后获取对应的Bean，比如上面方法三次获取的都是`BeanDefinitionRegistryPostProcessor`这个类型的`bd`。

我们可以思考一个问题，为什么这一步需要合并呢？大家可以带着这个问题继续往下看，在后文我会解释。

##### 2、实例化

Spring在实例化一个对象也会进行`bd`的合并。

第一次：

```java
org.springframework.beans.factory.support.DefaultListableBeanFactory#preInstantiateSingletons
public void preInstantiateSingletons() throws BeansException {
    // .....
	// 省略跟合并无关的代码
    for (String beanName : beanNames) {
        RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
        if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
            // .....
1234567
```

第二次：

```java
org.springframework.beans.factory.support.AbstractBeanFactory#doGetBean
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
                          @Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
    // .....
    // 省略跟合并无关的代码
    final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
    checkMergedBeanDefinition(mbd, beanName, args);

    // Guarantee initialization of beans that the current bean depends on.
    String[] dependsOn = mbd.getDependsOn();
    if (dependsOn != null) {
        // ....
    }
    if (mbd.isSingleton()) {
        // ....
    }
    // ....
12345678910111213141516
```

我们可以发现这两次合并有一个共同的特点，就是在**合并之后立马利用了合并之后的`bd`我们简称为`mbd`做了一系列的判断**，比如上面的`dependsOn != null`和`mbd.isSingleton()`。基于上面几个例子我们来分析：为什么需要合并？

#### 为什么需要合并？

在扫描阶段，之所以发生了合并，是因为Spring需要拿到指定了实现了`BeanDefinitionRegistryPostProcessor`接口的`bd`的名称，也就是说，Spring需要用到`bd`的名称。所以进行了一次`bd`的合并。在实例化阶段，是因为Spring需要用到`bd`中的一系列属性做判断所以进行了一次合并。我们总结起来，其实就是一个原因：**Spring需要用到`bd`的属性，要保证获取到的`bd`的属性是正确的**。

那么问题来了，为什么获取到的`bd`中属性可能不正确呢？

主要两个原因：

1. 作为子`bd`,属性本身就有可能缺失，比如我们在开头介绍的例子，子`bd`中本身就没有age属性，age属性在父`bd`中
2. Spring提供了很多扩展点，在启动容器的时候，可能会修改`bd`中的属性。比如一个正常实现了`BeanFactoryPostProcessor`就能修改容器中的任意的`bd`的属性。在后面的容器的扩展点中我再介绍

#### 合并的代码分析：

因为合并的代码其实很简单，所以一并在这里分析了，也可以加深对合并的理解：

```java
protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
    // Quick check on the concurrent map first, with minimal locking.
    // 从缓存中获取合并后的bd
    RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
    if (mbd != null) {
        return mbd;
    }
    // 如何获取不到的话，开始真正的合并
    return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
}
12345678910
	protected RootBeanDefinition getMergedBeanDefinition(
			String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)
			throws BeanDefinitionStoreException {

		synchronized (this.mergedBeanDefinitions) {
			RootBeanDefinition mbd = null;

			// Check with full lock now in order to enforce the same merged instance.
			if (containingBd == null) {
				mbd = this.mergedBeanDefinitions.get(beanName);
			}

			if (mbd == null) {
                // 如果没有parentName的话直接使用自身合并
                // 就是new了RootBeanDefinition然后再进行属性的拷贝
				if (bd.getParentName() == null) {
					if (bd instanceof RootBeanDefinition) {
						mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
					}
					else {   
						mbd = new RootBeanDefinition(bd);
					}
				}
				else {
					// 需要进行父子的合并
					BeanDefinition pbd;
					try {
						String parentBeanName = transformedBeanName(bd.getParentName());
						if (!beanName.equals(parentBeanName)) {
                            // 这里是递归，在将父子合并时，需要确保父bd已经合并过了
							pbd = getMergedBeanDefinition(parentBeanName);
						}
						else {
                            // 一般不会进这个判断
                            // 到父容器中找对应的bean，然后进行合并，合并也发生在父容器中
							BeanFactory parent = getParentBeanFactory();
							if (parent instanceof ConfigurableBeanFactory) {
								pbd = ((ConfigurableBeanFactory) parent).getMergedBeanDefinition(parentBeanName);
							}
							// 省略异常信息......
						}
					}
					// 省略异常信息......
					// 
					mbd = new RootBeanDefinition(pbd);
                    //用子bd中的属性覆盖父bd中的属性
					mbd.overrideFrom(bd);
				}

				// 默认设置为单例
				if (!StringUtils.hasLength(mbd.getScope())) {
					mbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
				}
                // 如果当前的bd是一个被嵌套bd,并且嵌套的bd不是单例的，但是当前的bd又是单例的
                // 那么将当前的bd的scope设置为嵌套bd的类型
				if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
					mbd.setScope(containingBd.getScope());
				}
				// 将合并后的bd放入到mergedBeanDefinitions这个map中
                // 之后还是可能被清空的，因为bd可能被修改
				if (containingBd == null && isCacheBeanMetadata()) {
					this.mergedBeanDefinitions.put(beanName, mbd);
				}
			}

			return mbd;
		}
	}
```

上面这段代码整体不难理解，可能发生疑惑的主要是两个点：

1. `pbd = getMergedBeanDefinition(parentBeanName);`

   这里进行的是父`bd`的合并，是方法的递归调用，这是因为在合并的时候父`bd`可能也还不是一个合并后的bd

2. `containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()`

我查了很久的资料，经过验证后发现，如果进行了形如下面的嵌套配置，那么`containingBd`会不为null

```xml
<bean id="luBanService" class="com.dmz.official.service.LuBanService" scope="prototype">
    <property name="lookUpService">
        <bean class="com.dmz.official.service.LookUpService" scope="singleton"></bean>
    </property>
</bean>
```

在这个例子中，`containingBd`为`LuBanService`，此时，`LuBanService`是一个原型的`bd`，但`lookUpService`是一个单例的`bd`，那么这个时候经过合并，`LookUpService`也会变成一个原型的`bd`。大家可以拿我这个例子测试一下。

#### 总结：

这篇文章我觉得最重要的是，我们要明白Spring为什么要进行合并，之所以再每次需要用到`BeanDefinition`都进行一次合并，是为了每次都拿到最新的，最有效的`BeanDefinition`，因为利用容器提供了一些扩展点我们可以修改`BeanDefinition`中的属性。关于容器的扩展点，比如上文提到了`BeanFactoryPostProcessor`以及`BeanDefinitionRegistryPostProcessor`,我会在后面的几篇文章中一一介绍。

`BeanDefinition`的学习就到这里了，这个类很重要，是整个Spring的基石，希望大家可以多花时间多研究研究相关的知识。加油，共勉！!

## Spring官网阅读（六）容器的扩展点（一）BeanFactoryPostProcessor

### 总览：

先看看官网是怎么说的：1.8.2![在这里插入图片描述](https://img-blog.csdnimg.cn/2020010700311376.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

从上面这段话，我们可以总结如下几点：

1. `BeanFactoryPostProcessor`可以对Bean配置元数据进行操作。也就是说，Spring容器允许`BeanFactoryPostProcessor`读取指定Bean的配置元数据，并可以在Bean被实例化之前修改它。这里说的配置元数据其实就是我们之前讲过的`BeanDefinition`。
2. 我们可以配置多个`BeanFactoryPostProcessor`，并且只要我们配置的`BeanFactoryPostProcessor`同时实现了`Ordered`接口的话，我们还可以控制这些`BeanFactoryPostProcessor`执行的顺序

接下来，我们通过Demo来感受下`BeanFactoryPostProcessor`的作用：

### 例子：

这里就以官网上的demo为例：

```xml
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
    <property name="locations" value="classpath:com/something/jdbc.properties"/>
</bean>

<bean id="dataSource" destroy-method="close" class="org.apache.commons.dbcp.BasicDataSource">
    <property name="driverClassName" value="${jdbc.driverClassName}"/>
    <property name="url" value="${jdbc.url}"/>
    <property name="username" value="${jdbc.username}"/>
    <property name="password" value="${jdbc.password}"/>
</bean>

# jdbc.properties
jdbc.driverClassName=org.hsqldb.jdbcDriver
jdbc.url=jdbc:hsqldb:hsql://production:9002
jdbc.username=sa
jdbc.password=root
```

在上面的例子中，我们配置了一个`PropertyPlaceholderConfigurer`,为了方便理解，我们先分析下这个类，其`UML`类图如下：

![在这里插入图片描述](/Users/weiliang/Desktop/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210129145903815.jpeg)

- `Ordered`用于决定执行顺序
- `PriorityOrdered`，这个接口直接继承了`Ordered`接口，并且没有做任何扩展。只是作为一个标记接口，也用于决定`BeanFactoryPostProcessor`的执行顺序。在后文源码分析时，我们会看到他的作用
- `Aware`相关的接口我们在介绍Bean的生命周期回调时统一再分析，这里暂且不管
- `FunctionalInterface`,这是`java8`新增的一个接口，也只是起一个标记作用，标记该接口是一个函数式接口。
- `BeanFactoryPostProcessor`，代表这个类是一个Bean工厂的后置处理器。
- `PropertiesLoaderSupport`,这个类主要包含定义了属性的加载方法，包含的属性如下：

```java
// 本地属性，可以直接在XML中配置
@Nullable
protected Properties[] localProperties;

// 是否用本地的属性覆盖提供的文件中的属性，默认不会
protected boolean localOverride = false;

// 根据地址找到的对应文件
@Nullable
private Resource[] locations;

// 没有找到对应文件是否抛出异常，false代表不抛出
private boolean ignoreResourceNotFound = false;

// 对应文件资源的编码
@Nullable
private String fileEncoding;

// 文件解析器
private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();
```

- `PropertyResourceConfigurer`，这个类主要可以对读取到的属性进行一些转换
- `PlaceholderConfigurerSupport`，主要负责对占位符进行解析。其中几个属性如下：

```java
// 默认解析的前缀
public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
// 默认解析的后缀
public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";
// 属性名称跟属性值的分隔符
public static final String DEFAULT_VALUE_SEPARATOR = ":";
```

- `PropertyPlaceholderConfigurer`继承了上面这些类的所有功能，同时可以配置属性的解析顺序

```java
// 不在系统属性中查找
public static final int SYSTEM_PROPERTIES_MODE_NEVER = 0;

// 如果在配置文件中没有找到，再去系统属性中查找
public static final int SYSTEM_PROPERTIES_MODE_FALLBACK = 1;

// 先查找系统属性，没查到再去查找配置文件中的属性
public static final int SYSTEM_PROPERTIES_MODE_OVERRIDE = 2;
```

对这个类有一些了解之后，我们回到之前的例子中，为什么在`jdbc.properties`文件中配置的属性值会被应用到`BasicDataSource`这个Bean上呢？为了帮助大家理解，我画了一个图：

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210129150310279.jpeg)

这个流程就如上图，可以看到我们通过`PropertyPlaceholderConfigurer`这个特殊的`BeanFactoryPostProcessor`完成了`BeanDefinition`中的属性值中的占位符的替换。在`BeanDefinition`被解析出来后，Bean实例化之前对其进行了更改了。

在上图中，创建Bean的过程我们暂且不管，还有一个问题我们需要弄清楚，Spring是如何扫描并解析成`BeanDefinition`的呢？这里就不得不提到我们接下来需要分析的这个接口了:``BeanDefinitionRegistryPostProcessor`。

### BeanDefinitionRegistryPostProcessor（重要）：

我们先来看一下这个接口的`UML`类图：

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210129150334959.jpeg)

从上图中，我们可以得出两个结论：

1. `BeanDefinitionRegistryPostProcessor`直接继承了`BeanFactoryPostProcessor`，所以它也是一个Bean工厂的后置处理器
2. Spring只提供了一个内置的`BeanDefinitionRegistryPostProcessor`的实现类，这个类就是`ConfigurationClassPostProcessor`，实际上我们上面说的扫描解析成`BeanDefinition`的过程就是由这个类完成的

我们来看下这个接口定义：

```java
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;
}

public interface BeanFactoryPostProcessor {
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
}
```

相比于正常的`BeanFactoryPostProcessor`，`BeanDefinitionRegistryPostProcessor`多提供了一个方法，那么多提供的这个方法有什么用呢？这个方法会在什么时候执行呢？这里我先说结论：

> 这个方法的左右也是为了扩展，相比于`BeanFactoryPostProcessor`的`postProcessBeanFactory`方法，这个方法的执行时机会更加靠前，Spring自身利用这个特性完成了`BeanDefinition`的扫描解析。我们在对Spring进行扩展时，也可以利用这个特性来完成扫描这种功能，比如最新版的`Mybatis`就是这么做的。关于`Mybatis`跟Spring整合的过程，我打算在写完Spring的扫描以及容器的扩展点这一系列文章后单独用一篇文章来进行分析。

接下来，我们直接分析其源码，验证上面的结论。

### 执行流程源码解析：

在分析源码前，我们看看下面这个图，以便大家对Spring的执行流程有个大概的了解：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107003218277.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

上图表示的是形如`AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(Config.class)`的执行流程。我们这次分析的代码主要是其中的`3-5-1`流程。对于的代码如下（代码比较长，我们拆分成两部分分析）：

### BeanDefinitionRegistryPostProcessor执行流程

```java
PostProcessorRegistrationDelegate.java
  
public static void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
    Set<String> processedBeans = new HashSet<>();
    // 这个if基本上一定会成立，除非我们手动new了一个beanFactory
    if (beanFactory instanceof BeanDefinitionRegistry) {
        
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
       
        // 存储了只实现了BeanFactoryPostProcessor接口的后置处理器
        List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
        
        // 存储了实现了BeanDefinitionRegistryPostProcessor接口的后置处理器
        List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
		
        // 这个beanFactoryPostProcessors集合一般情况下都是空的，除非我们手动调用容器的addBeanFactoryPostProcessor方法
        for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
            if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
                BeanDefinitionRegistryPostProcessor registryProcessor = (BeanDefinitionRegistryPostProcessor) postProcessor;
               
                // 执行实现了BeanDefinitionRegistryPostProcessor接口的后置处理器的postProcessBeanDefinitionRegistry方法，注意这里执行的不是postProcessBeanFactory方法，我们上面已经讲过了，实现了BeanDefinitionRegistryPostProcessor接口的后置处理器有两个方法，一个是从父接口中继承而来的postProcessBeanFactory方法，另一个是这个接口特有的postProcessBeanDefinitionRegistry方法
                registryProcessor.postProcessBeanDefinitionRegistry(registry);
               
                // 保存执行过了的BeanDefinitionRegistryPostProcessor，这里执行过的BeanDefinitionRegistryPostProcessor只是代表它的特有方法：postProcessBeanDefinitionRegistry方法执行过了，但是千万记得，它还有一个标准的postProcessBeanFactory，也就是从父接口中继承的方法还未执行
                registryProcessors.add(registryProcessor);
           
            } else {
               
                // 将只实现了BeanFactoryPostProcessor接口的后置处理器加入到集合中
                regularPostProcessors.add(postProcessor);
            }
        }
      
		    // 保存当前需要执行的实现了BeanDefinitionRegistryPostProcessor接口的后置处理器
        List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();
		    
        // 从容器中获取到所有实现了BeanDefinitionRegistryPostProcessor接口的Bean的名字
        String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
        for (String ppName : postProcessorNames) {
            // 判断这个类是否还实现了PriorityOrdered接口
            if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                // 如果满足条件，会将其创建出来，同时添加到集合中
                // 正常情况下，只会有一个，就是Spring容器自己提供的ConfigurationClassPostProcessor,Spring通过这个类完成了扫描以及BeanDefinition的功能
                currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                processedBeans.add(ppName);
            }
        }
      
        // 根据实现的PriorityOrdered接口进行排序
        sortPostProcessors(currentRegistryProcessors, beanFactory);
		
        // 将当前将要执行的currentRegistryProcessors全部添加到registryProcessors这个集合中
        registryProcessors.addAll(currentRegistryProcessors);
        
        // 执行后置处理器的逻辑，这里只会执行BeanDefinitionRegistryPostProcessor接口的postProcessBeanDefinitionRegistry方法
        invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
        
        // 清空集合
        currentRegistryProcessors.clear();
		
        // 这里重新获取实现了BeanDefinitionRegistryPostProcesso接口的后置处理器的名字，思考一个问题：为什么之前获取了一次不能直接用呢？还需要获取一次呢？这是因为，在我们上面执行过了BeanDefinitionRegistryPostProcessor中，可以在某个类中，我们扩展的时候又注册了一个实现了BeanDefinitionRegistryPostProcessor接口的后置处理器
        postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
        for (String ppName : postProcessorNames) {
            // 确保没有被处理过并且实现了Ordered接口
            if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
                // 加入到当前需要被执行的集合中
                currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                processedBeans.add(ppName);
            }
        }
       
        // 根据ordered接口进行排序
        sortPostProcessors(currentRegistryProcessors, beanFactory);
       
        // 将当前将要执行的currentRegistryProcessors全部添加到registryProcessors这个集合中
        registryProcessors.addAll(currentRegistryProcessors);
       
        // 执行后置处理器的逻辑，这里只会执行BeanDefinitionRegistryPostProcessor接口的postProcessBeanDefinitionRegistry方法
        invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
        
        // 清空集合
        currentRegistryProcessors.clear();
		
        // 接下来这段代码是为了确认所有实现了BeanDefinitionRegistryPostProcessor的后置处理器能够执行完，之所有要一个循环中执行，也是为了防止在执行过程中注册了新的BeanDefinitionRegistryPostProcessor
        boolean reiterate = true;
        while (reiterate) {
            reiterate = false;
            // 获取普通的BeanDefinitionRegistryPostProcessor，不需要实现PriorityOrdered或者Ordered接口
            postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
            for (String ppName : postProcessorNames) {
                if (!processedBeans.contains(ppName)) {
                    // 只要发现有一个需要执行了的后置处理器，就需要再次循环，因为执行了这个后置处理可能会注册新的BeanDefinitionRegistryPostProcessor
                    currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                    processedBeans.add(ppName);
                    reiterate = true;
                }
            }
            sortPostProcessors(currentRegistryProcessors, beanFactory);
            registryProcessors.addAll(currentRegistryProcessors);
            invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
            currentRegistryProcessors.clear();
        }
    ......
```

### BeanFactoryPostProcessor执行流程：

```java
	......承接上半部分代码......

        // 这里开始执行单独实现了BeanFactoryPostProcessor接口的后置处理器
    
        // 1.先执行实现了BeanDefinitionRegistryPostProcessor的BeanFactoryPostProcessor，在前面的逻辑中我们只执行了BeanDefinitionRegistryPostProcessor特有的postProcessBeanDefinitionRegistry方法，它的postProcessBeanFactory方法还没有被执行，它会在这里被执行
        invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

        // 2.执行直接实现了BeanFactoryPostProcessor接口的后置处理器
        invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
    } else {
      
		    // 正常情况下，进不来这个判断，不用考虑
        invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
    }

		// 获取所有实现了BeanFactoryPostProcessor接口的后置处理器，这里会获取到已经执行过的后置处理器，所以后面的代码会区分已经执行过或者未执行过
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// 保存直接实现了BeanFactoryPostProcessor接口和PriorityOrdered接口的后置处理器
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();

		// 保存直接实现了BeanFactoryPostProcessor接口和Ordered接口的后置处理器
		List<String> orderedPostProcessorNames = new ArrayList<>();

		// 保存直接实现了BeanFactoryPostProcessor接口的后置处理器，不包括那些实现了排序接口的类
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();

		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
        // 已经处理过了，直接跳过
			} else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
        // 符合条件，加入到之前申明的集合
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			} else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			} else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// 先执行实现了BeanFactoryPostProcessor接口和PriorityOrdered接口的后置处理器
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// 再执行实现了BeanFactoryPostProcessor接口和Ordered接口的后置处理器
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// 最后执行BeanFactoryPostProcessor接口的后置处理器，不包括那些实现了排序接口的类
		List<`1> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// 将合并的BeanDefinition清空，这是因为我们在执行后置处理器时，可能已经修改过了BeanDefinition中的属性，所以需要清空，以便于重新合并
		beanFactory.clearMetadataCache();
```

通过源码分析，我们可以将整个Bean工厂的后置处理器的执行流程总结如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107003228449.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

首先，要明白一点，上图分为左右两个部分，代表的不是两个接口，而是两个方法

- 一个是`BeanDefinitionRegistryPostProcesso`特有的`postProcessBeanDefinitionRegistry`方法
- 另外一个是`BeanFactoryPostProcessor`的`postProcessBeanFactory`方法

这里我们以方法为维度区分更好说明问题，`postProcessBeanDefinitionRegistry`方法的执行时机早于`postProcessBeanFactory`。并且他们按照上图从左到右的顺序进行执行。

另外在上面进行代码分析的时候不知道大家有没有发现一个问题，当在执行`postProcessBeanDefinitionRegistry`方法时，Spring采用了循环的方式，不断的查找是否有新增的`BeanDefinitionRegistryPostProcessor`，就是下面这段代码：

```java
boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

```

但是在执行`postProcessBeanFactory`并没有进行类似的查找。这是为什么呢？

笔者自己认为主要是设计使然，Spring在设计时`postProcessBeanFactory`这个方法不是用于重新注册一个Bean的，而是修改。我们可以看下这个方法上的这段`java doc`

```java
	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 * @param beanFactory the bean factory used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
```

其中最重要的一段话：`All bean definitions will have been loaded`，所有的`beanDefinition`都已经被加载了。

我们再对比下`postProcessBeanDefinitionRegistry`这个方法上的`java doc`

```java
	/**
	 * Modify the application context's internal bean definition registry after its
	 * standard initialization. All regular bean definitions will have been loaded,
	 * but no beans will have been instantiated yet. This allows for adding further
	 * bean definitions before the next post-processing phase kicks in.
	 * @param registry the bean definition registry used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;
```

大家注意这段话，`This allows for adding further bean definitions before the next post-processing phase kicks in.`允许我们在下一个后置处理器执行前添加更多的`BeanDefinition`

从这里，我相信大家更加能理解为什么`postProcessBeanDefinitionRegistry`这个方法的执行时机要早于`postProcessBeanFactory`了。

### 使用过程中的几个问题：

1、可不可以在BeanFactoryPostProcessor去创建一个Bean，这样有什么问题？

从技术上来说这样是可以的，但是正常情况下我们不该这样做，这是因为可能会存在该执行的Bean工厂的后置处理器的逻辑没有被应用到这个Bean上。

2、BeanFactoryPostProcessor可以被配置为懒加载吗？

不能配置为懒加载，即使配置了也不会生效。我们将Bean工厂后置处理器配置为懒加载这个行为就没有任何意义

### 总结：

在这篇文章中，我们最需要了解及掌握的就是`BeanFactoryPostProcessor`执行的顺序，总结如下：

- 先执行直接实现了BeanDefinitionRegistryPostProcessor接口的后置处理器，所有实现了BeanDefinitionRegistryPostProcessor接口的类有两个方法，一个是特有的postProcessBeanDefinitionRegistry方法，一个是继承子父接口的postProcessBeanFactory方法。

  - postProcessBeanDefinitionRegistry方法早于postProcessBeanFactory方法执行，对于postProcessBeanDefinitionRegistry

    的执行顺序又遵循如下原子

    1. 先执行实现了`PriorityOrdered`接口的类中的`postProcessBeanDefinitionRegistry`方法
    2. 再执行实现了`Ordered`接口的类中的`postProcessBeanDefinitionRegistry`的方法
    3. 最后执行没有实现上面两个接口的类中的`postProcessBeanDefinitionRegistry`的方法

  - 执行完所有的`postProcessBeanDefinitionRegistry`方法后，再执行实现了`BeanDefinitionRegistryPostProcesso`r接口的类中的`postProcessBeanFactory`方法

- 再执行直接实现了BeanFactoryPostProcessor接口的后置处理器

  1. 先执行实现了`PriorityOrdered`接口的类中的`postProcessBeanFactory`方法
  2. 再执行实现了`Ordered`接口的类中的`postProcessBeanFactory`的方法
  3. 最后执行没有实现上面两个接口的类中的`postProcessBeanFactory`的方法

## Spring官网阅读（七）容器的扩展点（二）FactoryBean

我们还是先看看官网上是怎么说的：1.8.3

### 官网介绍

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107003434762.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

从上面这段文字我们可以得出以下几个信息：

1. `FactoryBean`主要用来定制化Bean的创建逻辑
2. 当我们实例化一个Bean的逻辑很复杂的时候，使用`FactoryBean`是很必要的，这样可以规避我们去使用冗长的XML配置
3. `FactoryBean`接口提供了以下三个方法：

- `Object getObject()`: 返回这个`FactoryBean`所创建的对象。
- `boolean isSingleton()`: 返回`FactoryBean`所创建的对象是否为单例，默认返回true。
- `Class getObjectType()`: 返回这个`FactoryBean`所创建的对象的类型，如果我们能确认返回对象的类型的话，我们应该正常对这个方法做出实现，而不是返回null。

1. Spring自身大量使用了`FactoryBean`这个概念，至少有50个`FactoryBean`的实现类存在于Spring容器中
2. 假设我们定义了一个`FactoryBean`，名为`myFactoryBean`，当我们调用`getBean("myFactoryBean")`方法时返回的并不是这个`FactoryBean`，而是这个`FactoryBean`所创建的Bean，如果我们想获取到这个`FactoryBean`需要在名字前面拼接"&"，行如这种形式：`getBean("&myFactoryBean")`

上面这些概念可能刚刚说的时候大家不是很明白，下面我们通过`FactoryBean`的一些应用来进一步体会这个接口的作用。

### FactoryBean的应用

我们来看下面这个Demo:

```java
public class MyFactoryBean implements FactoryBean {
	@Override
	public Object getObject() throws Exception {
		System.out.println("执行了一段复杂的创建Bean的逻辑");
		return new TestBean();
	}

	@Override
	public Class<?> getObjectType() {
		return TestBean.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}

public class TestBean {
	public TestBean(){
		System.out.println("TestBean被创建出来了");
	}
}
// 测试类
public class Main {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac=new AnnotationConfigApplicationContext(Config.class);
		System.out.println("直接调用getBean(\"myFactoryBean\")返回："+ac.getBean("myFactoryBean"));
		System.out.println("调用getBean(\"&myFactoryBean\")返回："+ac.getBean("&myFactoryBean"));
	}
}
```

运行后结果如下：

------

> 执行了一段复杂的创建Bean的逻辑
> TestBean被创建出来了
> 直接调用getBean(“myFactoryBean”)返回：com.dmz.official.extension.factorybean.TestBean@28f67ac7
> 调用getBean("&myFactoryBean")返回：com.dmz.official.extension.factorybean.MyFactoryBean@256216b3

------

我们虽然没有直接将`TestBean`放入Spring容器中，但是通过`FactoryBean`也完成了这一操作。同时当我们直接调用`getBean("FactoryBean的名称")`获取到的是`FactoryBean`创建的Bean，但是添加了“&”后可以获取到`FactoryBean`本身。

### FactoryBean相关源码分析

我们先看下下面这张图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107003448877.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

涉及到`FactoryBean`主要在`3-11-6`这一步中，我们主要关注下面这段代码：

```java
// .....省略无关代码.......

// 1.判断是不是一个FactoryBean
if (isFactoryBean(beanName)) {
    // 2.如果是一个FactoryBean那么在getBean时，添加前缀“&”，获取这个FactoryBean
    Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
    if (bean instanceof FactoryBean) {
        final FactoryBean<?> factory = (FactoryBean<?>) bean;
        boolean isEagerInit;
        // 3.做权限校验，判断是否是一个SmartFactoryBean，并且不是懒加载的
        if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
            isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
                                                        ((SmartFactoryBean<?>) factory)::isEagerInit,
                                                        getAccessControlContext());
        }
        else {
            // 3.判断是否是一个SmartFactoryBean，并且不是懒加载的
            isEagerInit = (factory instanceof SmartFactoryBean &&
                           ((SmartFactoryBean<?>) factory).isEagerInit());
        }
        if (isEagerInit) {
            // 4.如果是一个SmartFactoryBean并且不是懒加载的，那么创建这个FactoryBean创建的Bean
            getBean(beanName);
        }
    }
}
else {
    // 不是一个FactoryBean，直接创建这个Bean
    getBean(beanName);
}
// ...省略无关代码.....
```

我们按照顺序一步步分析，首先看第一步:

1. 判断是不是一个`FactoryBean`，对应源码如下：

```java
public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
    String beanName = transformedBeanName(name);
    // 直接从单例池中获取这个Bean，然后进行判断，看是否是一个FactoryBean
    Object beanInstance = getSingleton(beanName, false);
    if (beanInstance != null) {
        return (beanInstance instanceof FactoryBean);
    }
    // 查找不到这个BeanDefinition，那么从父容器中再次确认是否是一个FactoryBean
    if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
        // No bean definition found in this factory -> delegate to parent.
        return ((ConfigurableBeanFactory) getParentBeanFactory()).isFactoryBean(name);
    }
    // 从当前容器中，根据BeanDefinition判断是否是一个FactoryBean
    return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
}
```

1. 如果是一个`FactoryBean`那么在`getBean`时，添加前缀“&”，获取这个`FactoryBean`
2. 判断是否是一个`SmartFactoryBean`，并且不是懒加载的

这里涉及到一个概念，就是`SmartFactoryBean`，实际上这个接口继承了`FactoryBean`接口，并且`SmartFactoryBean`是`FactoryBean`的唯一子接口，它扩展了`FactoryBean`多提供了两个方法如下：

```java
// 是否为原型，默认不是原型
default boolean isPrototype() {
    return false;
}

// 是否需要提早生产（eager急切的、迫切的），默认为不提前生产。
default boolean isEagerInit() {
    return false;
}
```

从上面的代码中可以看出，我们当实现一个`FactoryBean`接口，Spring并不会在启动时就将这个`FactoryBean`所生产的Bean创建出来，为了避免这种情况，我们有两种办法：

- 实现`SmartFactoryBean`，并重写`isEagerInit`方法，将返回值设置为true
- 我们也可以在一个不是懒加载的Bean中注入这个`FactoryBean`所创建的Bean，Spring在解决依赖关系也会帮我们将这个Bean创建出来

实际上我们可以发现，当我们仅仅实现`FactoryBean`时，其`getObject()`方法所产生的Bean，我们可以当前是懒加载的。

1. 如果是一个`SmartFactoryBean`并且不是懒加载的，那么创建这个`FactoryBean`创建的Bean。这里需要注意的是此时创建的不是这个`FactoryBean`，因为在`getBean`时并没有加一个前缀“&”，所以获取到的是其`getObject()`方法所产生的Bean。

在上面的代码分析完后，在`3-6-11-2`中也有两行`FactoryBean`相关的代码，如下：

```java
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
                          @Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
	
    // 1.获取bean名称
    final String beanName = transformedBeanName(name);
    Object bean;
	
    //...省略无关代码...，这里主要根据beanName创建对应的Bean
	
    // 2.调用getObject对象创建Bean
        bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
    }
```

1. 获取bean名称

```java
protected String transformedBeanName(String name) {
    // 這個方法主要用來解析別名，如果是別名的話，获取真实的BeanName
    return canonicalName(BeanFactoryUtils.transformedBeanName(name));
}

 // 处理FactoryBean
public static String transformedBeanName(String name) {
    Assert.notNull(name, "'name' must not be null");
    // 没有带“&”，直接返回
    if (!name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
        return name;
    }
    // 去除所有的“&”，防止这种写法getBean("&&&&beanName")
    return transformedBeanNameCache.computeIfAbsent(name, beanName -> {
        do {
            beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
        }
        while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
        return beanName;
    });
}
```

1. 如果是一个`FactoryBean`，将会调用其`getObject()`方法，如果不是直接返回。

我们可以看到，在调用`getObjectForBeanInstance(sharedInstance, name, beanName, null);`传入了一个参数—name，也就是还没有经过`transformedBeanName`方法处理的bean的名称，可能会带有“&”符号，Spring通过这个参数判断这个Bean是不是一个`FactoryBean`,如果是的话，会调用其`getObject()`创建Bean。**被创建的Bean不会存放于单例池中，而是放在一个名为`factoryBeanObjectCache`的缓存中。**具体的代码因为比较复杂，在这里我们就暂且不分析了，大家可以先留个印象，源码阶段我会做详细的分析。

### Spring中*FactoryBean*概念的汇总（纯粹个人观点）

除了我们在上文中说到的实现了`FactoryBean`或者`SmartFactoryBean`接口的Bean可以称之为一个”*`FactoryBean`*“，不知道大家对`BeanDefinition`中的一个属性是否还有印象。`BeanDefinition`有属性如下（实际上这个属性存在于`AbstractBeanDefinition`中）：

```java
@Nullable
private String factoryBeanName;
@Nullable
private String factoryMethodName;
```

对于这个属性跟我们这篇文章中介绍的`FactoryBean`有什么关系呢？

首先，我们看看什么情况下`bd`中会存在这个属性，主要分为以下两种情况：

**第一种情况：**

```java
@Configuration
public class Config {
	@Bean
	public B b(){
		return new B();
	}
}
```

我们通过`@Bean`的方式来创建一个Bean，那么在B的`BeanDefinition`会记录`factoryBeanName`这个属性，同时还会记录是这个Bean中的哪个方法来创建B的。在上面的例子中，`factoryBeanName`=`config`，`factoryMethodName`=b。

**第二种情况：**

```xml
<bean id="factoryBean" class="com.dmz.official.extension.factorybean"/>

<bean id="b" class="com.dmz.official.extension.factorybean.B" factory-bean="factoryBean" factory-method="b"/>
```

通过XML的方式进行配置，此时B的`BeanDefinition`中`factoryBeanName`=`factoryBean`，`factoryMethodName`=b。

上面两种情况，`BeanDefinition`中的`factoryBeanName`这个属性均不会为空，但是请注意此时记录的这个名字所对的Bean并不是一个实现了`FactoryBean`接口的Bean。

综上，我们可以将Spring中的`FactoryBean`的概念泛化，也就是说所有生产对象的Bean我们都将其称为`FactoryBean`，那么可以总结画图如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107003503263.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

这是个人观点哈，没有在官网找到什么文档，只是这种比较学习更加能加深印象，所以我把他们做了一个总结，大家面试的时候不用这么说![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107003522152.png)

### 跟FactoryBean相关常见的面试题

1、FactoryBean跟BeanFactory的区别

`FactoryBean`就如我们标题所说，是Spring提供的一个扩展点，适用于复杂的Bean的创建。**`mybatis`在跟Spring做整合时就用到了这个扩展点**。并且`FactoryBean`所创建的Bean跟普通的Bean不一样。我们可以说`FactoryBean`是Spring创建Bean的另外一种手段。

而`BeanFactory`是什么呢？`BeanFactory`是`Spring IOC`容器的顶级接口，其实现类有`XMLBeanFactory`，`DefaultListableBeanFactory`以及`AnnotationConfigApplicationContext`等。`BeanFactory`为Spring管理Bean提供了一套通用的规范。接口中提供的一些方法如下：

```java
boolean containsBean(String beanName)

Object getBean(String)

Object getBean(String, Class)

Class getType(String name)

boolean isSingleton(String)

String[] getAliases(String name)
1234567891011
```

通过这些方法，可以方便地获取bean，对Bean进行操作和判断。

2、如何把一个对象交给Spring管理

首先，我们要弄明白一点，这个问题是说，怎么把一个**对象**交給Spring管理，“对象”要划重点，我们通常采用的注解如`@Compent`或者XML配置这种类似的操作并不能将一个对象交给Spring管理，而是让Spring根据我们的配置信息及类信息创建并管理了这个对象，形成了Spring中一个Bean。把一个对象交给Spring管理主要有两种方式

- 就是用我们这篇文章中的主角，`FactoryBean`，我们直接在`FactoryBean`的`getObject`方法直接返回需要被管理的对象即可
- `@Bean`注解，同样通过`@Bean`注解标注的方法直接返回需要被管理的对象即可。

### 总结

在本文中我们完成了对`FactoryBean`的学习，最重要的是我们需要明白一点，`FactoryBean`是Spring中特殊的一个Bean，Spring利用它提供了另一种创建Bean的方式，`FactoryBean`整体的体系比较复杂，`FactoryBean`是如何创建一个Bean的一些细节我们还没有涉及到，不过不要急，在源码学习阶段我们还会接触到它，并会对其的整个流程做进一步的分析。目前容器的扩展点我们还剩最后一个部分，即`BeanPostProcessor`。`BeanPostProcessor`贯穿了整个Bean的生命周期，学习的难度更大。希望大家跟我一步步走下去，认认真真学习完Spring，加油！

## Spring官网阅读（八）容器的扩展点（三）（BeanPostProcessor）

按照惯例，我们先看看官网对 `BeanPostProcessor`的介绍。1.8.1

### 官网介绍

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200107003726907.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

从这段文字中，我们能获取到如下信息：

1. `BeanPostProcessor`接口定义了两个回调方法，通过实现这两个方法我们可以提供自己的实例化以及依赖注入等逻辑。而且，如果我们想要在Spring容器完成实例化，配置以及初始化一个Bean后进行一些定制的逻辑，我们可以插入一个甚至更多的`BeanPostProcessor`的实现。
2. 我们可以配置多个`BeanPostProcessor`，并且只要我们配置的`BeanPostProcessor`同时实现了`Ordered`接口的话，我们还可以控制这些`BeanPostProcessor`执行的顺序

我们通过一个例子来看看`BeanPostProcessor`的作用

### 应用举例

Demo如下：

```java
// 自己实现了一个BeanPostProcessor
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (beanName.equals("indexService")) {
			System.out.println(bean);
			System.out.println("bean config invoke postProcessBeforeInitialization");
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (beanName.equals("indexService")) {
			System.out.println(bean);
			System.out.println("bean config invoke postProcessAfterInitialization");
		}
		return bean;
	}
}

@Component
public class IndexService {
    @Autowired
    LuBanService luBanService;

    @Override
    public String toString() {
        return "IndexService{" +
            "luBanService=" + luBanService +
            '}';
    }
}

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(Config.class);
    }
}
```

运行上面的程序：

```
IndexService{luBanService=com.dmz.official.extension.entity.LuBanService@5e025e70}
bean config invoke postProcessBeforeInitialization
IndexService{luBanService=com.dmz.official.extension.entity.LuBanService@5e025e70}
bean config invoke postProcessAfterInitialization
```

*从上面的执行结果我们可以得出一个结论，`BeanPostProcessor`接口中的两个方法的执行时机在属性注入之后。*因为从打印的结果我们可以发现，`IndexService`中的`luBanService`属性已经被注入了。

### 接口继承关系

由于`BeanPostProcessor`这个接口Spring本身内置的实现类就有很多，所以这里我们暂且不分析其实现类，就从接口的定义上来分析它的作用，其接口的`UML`类图如下：

![在这里插入图片描述](/Users/weiliang/Desktop/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210129163210878-20210129163353274.png)

1、`BeanPostProcessor`，这个接口是我们Bean的后置处理器的顶级接口，其中主要包含了两个方法

```java
// 在Bean初始化前调用
@Nullable
default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
}
// 在Bean初始化前调用
@Nullable
default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
}
```

2、`InstantiationAwareBeanPostProcessor`，继承了`BeanPostProcessor`接口，并在此基础上扩展了4个方法，其中方法`postProcessPropertyValues`已经在5.1版本中被废弃了

```java
// 在Bean实例化之前调用
@Nullable
default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
    return null;
}
// 在Bean实例化之后调用
default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
    return true;
}

// 我们采用注解时，Spring通过这个方法完成了属性注入
@Nullable
default PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
    throws BeansException {
    return null;
}

// 在5.1版本中已经被废弃了 
@Deprecated
@Nullable
default PropertyValues postProcessPropertyValues(
    PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
    return pvs;
}
```

大部分情况下我们在扩展时都不会用到上面的`postProcessProperties`跟`postProcessPropertyValues`，如果在某些场景下，不得不用到这两个方法，那么请注意，在实现`postProcessProperties`必须返回null，否则`postProcessPropertyValues`方法的逻辑不会执行。

3、`SmartInstantiationAwareBeanPostProcessor`，继续扩展了上面的接口，并多提供了三个方法

```java
// 预测Bean的类型，主要是在Bean还没有创建前我们可以需要获取Bean的类型
@Nullable
default Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
    return null;
}
// Spring使用这个方法完成了构造函数的推断
@Nullable
default Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName)
    throws BeansException {

    return null;
}
// 主要为了解决循环依赖，Spring内部使用这个方法主要是为了让早期曝光的对象成为一个“合格”的对象
default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
    return bean;
}
```

这个接口中的三个方法一般是在Spring内部使用，我们可以关注上这个接口上的一段`java doc`

```
This interface is a special purpose interface, mainly for
internal use within the framework. In general, application-provided
post-processors should simply implement the plain {@link BeanPostProcessor}
interface or derive from the {@link InstantiationAwareBeanPostProcessorAdapter}
class
```

上面这段文字很明确的指出了这个接口的设计是为了一些特殊的目的，主要是在Spring框架的内部使用，通常来说我们提供的Bean的后置处理器只要实现`BeanPostProcessor`或者`InstantiationAwareBeanPostProcessorAdapter`即可。正常情况下，我们在扩展时不需要考虑这几个方法。

4、`DestructionAwareBeanPostProcessor`，这个接口直接继承了`BeanPostProcessor`，同时多提供了两个方法，主要用于Bean在进行销毁时进行回调

```java
// 在Bean被销毁前调用
void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;
// 判断是否需要被销毁，默认都需要
default boolean requiresDestruction(Object bean) {
    return true;
}
```

5、`MergedBeanDefinitionPostProcessor`，这个接口也直接继承了`BeanPostProcessor`，但是多提供了两个方法。

```java
// Spring内部主要使用这个方法找出了所有需要注入的字段，同时做了缓存
void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);

// 主要用于在BeanDefinition被修改后，清除容器中的缓存
default void resetBeanDefinition(String beanName) {
}
```

### 源码分析

我们带着两个问题去阅读源码：

1. 容器中这么多`BeanPostProcessor`，它们会按什么顺序执行呢？
2. `BeanPostProcessor`接口中这么多方法，它们的执行时机是什么时候呢？

接下来我们解决这两个问题

**执行顺序**

在Spring内部，当去执行一个`BeanPostProcessor`一般都是采用下面这种形式的代码：

```java
for (BeanPostProcessor bp : getBeanPostProcessors()) {
    // 判断属于某一类后置处理器
    if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
       	// 执行逻辑
        }
    }
}
```

`getBeanPostProcessors()`获取到的`BeanPostProcessor`其实就是一个list集合，所以我们要分析`BeanPostProcessor`的执行顺序，其实就是分析这个list集合中的数据是通过什么样的顺序添加进来的，我们来看看之前说的一个Spring的执行流程图：

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210129173536649.png)
我们这次要分析的代码就是其中的`3-6`步骤，代码如下：

```java
public static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
	  // 1.获取容器中已经注册的Bean的名称，根据BeanDefinition中获取BeanName
    String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);
    // 2.通过addBeanPostProcessor方法添加的BeanPostProcessor以及注册到容器中的BeanPostProcessor的总数量
    int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
    // 3.添加一个BeanPostProcessorChecker，主要用于日志记录
    beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));
	
    // 保存同时实现了BeanPostProcessor跟PriorityOrdered接口的后置处理器
    List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
    // 保存实现了MergedBeanDefinitionPostProcessor接口的后置处理器
    List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
    // 保存同时实现了BeanPostProcessor跟Ordered接口的后置处理器的名字
    List<String> orderedPostProcessorNames = new ArrayList<>();
    // 保存同时实现了BeanPostProcessor但没有实现任何排序接口的后置处理器的名字
    List<String> nonOrderedPostProcessorNames = new ArrayList<>();
    
    // 4.遍历所有的后置处理器的名字，并根据不同类型将其放入到上面申明的不同集合中 
    // 同时会将实现了PriorityOrdered接口的后置处理器创建出来
    // 如果实现了MergedBeanDefinitionPostProcessor接口，放入到internalPostProcessors
    for (String ppName : postProcessorNames) {
        if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
            BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
            priorityOrderedPostProcessors.add(pp);
            if (pp instanceof MergedBeanDefinitionPostProcessor) {
                internalPostProcessors.add(pp);
            }
        } else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
            orderedPostProcessorNames.add(ppName);
        } else {
            nonOrderedPostProcessorNames.add(ppName);
        }
    }

   	// 5.将priorityOrderedPostProcessors集合排序
    sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
    // 6.将priorityOrderedPostProcessors集合中的后置处理器添加到容器中
    registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

    // 7.遍历所有实现了Ordered接口的后置处理器的名字，并进行创建
    // 如果实现了MergedBeanDefinitionPostProcessor接口，放入到internalPostProcessors
    List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
    for (String ppName : orderedPostProcessorNames) {
        BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
        orderedPostProcessors.add(pp);
        if (pp instanceof MergedBeanDefinitionPostProcessor) {
            internalPostProcessors.add(pp);
        }
    }
    // 排序及将其添加到容器中
    sortPostProcessors(orderedPostProcessors, beanFactory);
    registerBeanPostProcessors(beanFactory, orderedPostProcessors);

    // 7.遍历所有实现了常规后置处理器（没有实现任何排序接口）的名字，并进行创建
    // 如果实现了MergedBeanDefinitionPostProcessor接口，放入到internalPostProcessors 
    List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
    for (String ppName : nonOrderedPostProcessorNames) {
        BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
        nonOrderedPostProcessors.add(pp);
        if (pp instanceof MergedBeanDefinitionPostProcessor) {
            internalPostProcessors.add(pp);
        }
    }
    // 8.这里需要注意下，常规后置处理器不会调用sortPostProcessors进行排序
    registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

    // 9.对internalPostProcessors进行排序并添加到容器中
    sortPostProcessors(internalPostProcessors, beanFactory);
    registerBeanPostProcessors(beanFactory, internalPostProcessors);

    // 10.最后添加的这个后置处理器主要为了可以检测到所有的事件监听器
    beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
}
```

### 疑惑代码解读

下面对上述代码中大家可能存疑的地方进行一波分析：

1、获取容器中已经注册的Bean的名称，根据`BeanDefinition`中获取`BeanName`

> 这里主要是根据已经注册在容器中的`BeanDefinition`，这些`BeanDefinition`既包括程序员自己注册到容器中的，也包括Spring自己注册到容器中。注意这些后置处理器目前没有被创建，只是以`BeanDefinition`的形式存在于容器中,所以如果此时调用`getBeanPostProcessors()`，是拿不到这些后置处理器的，至于容器什么时候注册了后置处理器的`BeanDefinition`，大家可以先自行阅读`1-1`步骤的源码，我在后续文章中会分析，当前就暂时先跳过了

2、通过`addBeanPostProcessor`方法添加的`BeanPostProcessor`以及注册到容器中的`BeanPostProcessor`的总数量

> 这里主要是获取容器中已经存在的`BeanPostProcessor`的数量再加上已经被扫描出`BeanDefinition`的后置处理器的数量（这些后置处理器还没有被创建出来），最后再加1。这里就有两个问题

- 容器中已经存在的`BeanPostProcessor`是从哪里来的？

分为两个来源，第一，容器启动时，自身调用了`addBeanPostProcessor`添加了后置处理器；第二，程序员手动调用了`addBeanPostProcessor`方法添加了后置处理器，第二种情况很少见，代码形如下面这种形式：

```java
AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
ac.register(Config.class);
ac.getBeanFactory().addBeanPostProcessor(new MyBeanPostProcessor());
ac.refresh();
```

容器又是在什么时候添加的后置处理器呢？大家可以自己阅读第`3-3`步骤的源码。在第`3-5`步也添加了一个后置处理器，由于代码比较深，不建议大家现在去看，关注我后续的更新即可，这些问题都会在后面的文章中解决

- 为什么最后还需要加1呢?

这个就跟我们将要分析的`3-6`步骤第三行代码相关

```java
// 3.添加一个BeanPostProcessorChecker，主要用于日志记录
beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount)); 
```

3、添加一个`BeanPostProcessorChecker`，主要用于日志记录

我们看下`BeanPostProcessorChecker`这个类的源码：

```java
private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName)
					&& this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName()
							+ "] is not eligible for getting processed by all BeanPostProcessors "
							+ "(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}

	}
```

这段代码中我们主要需要关注的就是以下两个方法：

- `isInfrastructureBean`，这个方法主要检查当前处理的Bean是否是一个Spring自身需要创建的Bean，而不是程序员所创建的Bean（通过`@Component`,`@Configuration`等注解或者XML配置等）。
- `postProcessAfterInitialization`，我们可以看到这个方法内部只是做了一个判断，只要当前创建的Bean不是一个后置处理器并且不是一个Spring自身需要创建的基础的Bean，最后还有一个判断`this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount`，这是什么意思呢？其实意思就是说在创建这个Bean时容器中的后置处理器还没有完全创建完。这个判断也能解释我们上面遗留的一个[问题](https://daimingzhi.blog.csdn.net/article/details/103867048#jump)。之所以要加1是为了方便判断，否则还需要进行等号判断。

4、上面代码段落中标注的`3-6`步骤`4到7`之间的代码就不解释了，比较简单，可能大家需要注意的就是在`registerBeanPostProcessors`方法中调用了一个`addBeanPostProcessor(BeanPostProcessor beanPostProcessor)`方法，我们看下这个方法的执行逻辑：

```java
public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
    Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
    // 可以看到，后添加进来的beanPostProcessor会覆盖之前添加的
    this.beanPostProcessors.remove(beanPostProcessor);
    // 这个状态变量会影响之后的执行流程，我们只需知道一旦添加了一个InstantiationAwareBeanPostProcessor就会将变量置为true即可
    if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
        this.hasInstantiationAwareBeanPostProcessors = true;
    }
    if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
        this.hasDestructionAwareBeanPostProcessors = true;
    }
    this.beanPostProcessors.add(beanPostProcessor);
}
```

5、注意下`第8段`代码，对于没有实现任何排序接口的后置处理器，Spring是不会进行排序操作的。即使你添加了`@Order`注解也没有任何作用。这里只是针对Spring Framework。

6、`第10段`代码中又添加了一个后置处理器`ApplicationListenerDetector`，添加的这个后置处理器主要为了可以检测到所有的事件监听器，我们看下它的代码（这里只分析下它的几个核心方法）：

```java
ApplicationListenerDetector.java

// 1.singletonNames保存了所有将要创建的Bean的名称以及这个Bean是否是单例的映射关系
// 这个方法会在对象被创建出来后，属性注入之前执行
public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
    this.singletonNames.put(beanName, beanDefinition.isSingleton());
}

// 2.整个Bean创建过程中最后一个阶段执行，在对象被初始化后执行
@Override
public Object postProcessAfterInitialization(Object bean, String beanName) {
    if (bean instanceof ApplicationListener) {
       // 3.判断当前这个Bean是不是单例，如果是的话，直接添加到容器的监听器集合中
        Boolean flag = this.singletonNames.get(beanName);
        if (Boolean.TRUE.equals(flag)) {
            // 添加到容器的监听器集合中
            this.applicationContext.addApplicationListener((ApplicationListener<?>) bean);
        }
        // 4.如果不是单例的，并且又是一个嵌套的Bean，那么打印日志，
        //   提示用户也就是程序员内嵌的Bean只有在单例的情况下才能作为时间监听器
        else if (Boolean.FALSE.equals(flag)) {
            if (logger.isWarnEnabled() && !this.applicationContext.containsBean(beanName)) {
                // inner bean with other scope - can't reliably process events
                logger.warn("Inner bean '" + beanName + "' implements ApplicationListener interface " +
                        "but is not reachable for event multicasting by its containing ApplicationContext " +
                        "because it does not have singleton scope. Only top-level listener beans are allowed " +
                        "to be of non-singleton scope.");
            }
            this.singletonNames.remove(beanName);
        }
    }
    return bean;
}
```

这个后置处理器主要是针对事件监听器（Spring中的事件监听机制在后续文章中会做介绍，这里如果有的同学不知道的话只需要暂时记住有这个概念即可，把它当作Spring中一个特殊的Bean即可）。上面代码中的`第3步`跟`第4步`可能会让人比较迷惑，实际上在我之前画的执行流程图中的`3-10`步，Spring就已经注册过了一次监听器，在`3-10`步骤中，其实Spring已经通过`String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false)`这段代码拿到了所有的名字，**那么我们思考一个问题，为什么Spring不直接根据这些名字去过滤创建的Bean，而要通过一个特定的后置处理器去进行处理呢？**比如可以通过下面这种逻辑：

```java
if(listenerBeanNames.contains(beanName)){
	 this.applicationContext.addApplicationListener(bean);
}
```

这里主要是因为一种特殊的Bean，它会由Spring来创建，自身确不在Spring容器中，这种特殊的Bean就是嵌套bean。注意这里说的是**嵌套Bean**，不是内部类，是由形如下面的XML配置的Bean

```xml
<bean id="indexService" class="com.dmz.official.service.IndexService">
    <property name="luBanService">
        <bean class="com.dmz.official.service.LuBanService"/>
    </property>
    <property name="dmzService" ref="dmzService"/>
</bean>
```

在上面的例子中，`LuBanService`就是一个嵌套的Bean。

假设我们上面的`LuBanService`是一个事件监听器，那么在`getBeanNamesForType`这个方法执行时，是无法获取到这个Bean的名称的。所以Spring专门提供了上述的那个后置处理器，用于处理这种嵌套Bean的情况，但是所提供的嵌套Bean必须要是单例的。

在分析执行时机时，我们先要知道Spring在创建一个Bean时要经历过哪些阶段，这里其实涉及到Bean的生命周期了，在下篇文章中我会专门分析Spring的生命周期，这篇文章中为了说明后置处理器的执行时机，先进行一些大致的介绍，整体创建Bean的流程可以画图如下：

### 总结

在这篇文章中，我们学习过了Spring中最后一个扩展点`BeanPostProcessor`，通过这篇文章，我们算是对`BeanPostProcessor`中的方法以及执行顺序有了大致的了解，但是到目前为止我们还不知道每个方法具体的执行时机是什么时候，这个问题我打算把它放到下篇文章中，结合官网中关于Spring生命周期回调方法的相关内容一起分析。到此为止对于官网中提到了三个容器的扩展点就学习完了，可以简单总结如下：

1、`BeanPostProcessor`，主要用于干预Bean的创建过程。

2、`BeanFactroyPostProcessor`，主要用于针对容器中的`BeanDefinition`

3、`FactoryBean`，主要用于将一个对象直接放入到Spring容器中，同时可以封装复杂的对象的创建逻辑



## Spring官网阅读（九）Spring中Bean的生命周期（上）

> 在之前的文章中，我们一起学习过了官网上容器扩展点相关的知识，包括`FactoryBean`，`BeanFactroyPostProcessor`,`BeanPostProcessor`，其中`BeanPostProcessor`还剩一个很重要的知识点没有介绍，就是相关的`BeanPostProcessor`中的方法的执行时机。之所以在之前的文章中没有介绍是因为这块内容涉及到Bean的生命周期。在这篇文章中我们开始学习Bean的生命周期相关的知识，整个Bean的生命周期可以分为以下几个阶段：
>
> - 实例化（得到一个还没有经过属性注入跟初始化的对象）
> - 属性注入（得到一个经过了属性注入但还没有初始化的对象）
> - 初始化（得到一个经过了初始化但还没有经过`AOP`的对象，`AOP`会在后置处理器中执行）
> - 销毁
>
> 在上面几个阶段中，`BeanPostProcessor`将会穿插执行。而在初始化跟销毁阶段又分为两部分：
>
> - 生命周期回调方法的执行
> - aware相关接口方法的执行
>
> 这篇文章中，我们先完成Bean生命周期中，整个初始化阶段的学习，对于官网中的章节为**`1.6`小结**

### 生命周期回调

#### 1、Bean初始化回调

实现初始化回调方法，有以下三种形式

##### 实现`InitializingBean`接口

如下：

```java
public class AnotherExampleBean implements InitializingBean {

    public void afterPropertiesSet() {
        // do some initialization work
    }
}
```

##### 使用Bean标签中的`init-method`属性

配置如下：

```xml
<bean id="exampleInitBean" class="examples.ExampleBean" init-method="init"/>
```

~~~java
public class ExampleBean {

    public void init() {
        // do some initialization work
    }
}
~~~

##### 使用`@PostConstruct`注解

配置如下：

```java
public class ExampleBean {
	@PostConstruct
    public void init() {
        // do some initialization work
    }
}
```

#### 2、Bean销毁回调

实现销毁回调方法，有以下三种形式

##### 实现`DisposableBean`接口

```java
public class AnotherExampleBean implements DisposableBean {

    public void destroy() {
        // do some destruction work (like releasing pooled connections)
    }
}
```

##### 使用Bean标签中的`destroy-method`属性

```java
<bean id="exampleInitBean" class="examples.ExampleBean" destroy-method="cleanup"/>
```

```java
public class ExampleBean {
    public void cleanup() {
        // do some destruction work (like releasing pooled connections)
    }
}
```

##### 使用`@PreDestroy`注解

```java
public class ExampleBean {
	@PreDestroy
    public void cleanup() {
        // do some destruction work (like releasing pooled connections)
    }
}
```

#### 3、配置默认的初始化及销毁方法

我们可以通过如下这种配置，为多个Bean同时指定初始化或销毁方法

```xml
<beans default-init-method="init" default-destroy-method="destory">
    <bean id="blogService" class="com.something.DefaultBlogService">
        <property name="blogDao" ref="blogDao" />
    </bean>
</beans>
```

在上面的XML配置中，Spring会将所有处于`beans`标签下的Bean的初始化方法名默认为`init`，销毁方法名默认为`destory`。

但是如果我们同时在bean标签中也指定了`init-method`属性，那么默认的配置将会被覆盖。

#### 4、执行顺序

如果我们在配置中同时让一个Bean实现了回调接口，又在Bean标签中指定了初始化方法，还进行了
`@PostContruct`注解的配置的话，那么它们的执行顺序如下：

1. 被`@PostConstruct`所标记的方法
2. `InitializingBean` 接口中的`afterPropertiesSet()` 方法
3. Bean标签中的 `init()`方法

对于销毁方法执行顺序如下：

1. 被`@PreDestroy`所标记的方法
2. `destroy()` `DisposableBean` 回调接口中的`destroy()`方法
3. Bean标签中的 `destroy()`方法

我们可以总结如下：

注解的优先级 > 实现接口的优先级 > XML配置的优先级

**同时我们需要注意的是，官网推荐我们使用注解的形式来定义生命周期回调方法，这是因为相比于实现接口，采用注解这种方式我们的代码跟Spring框架本身的耦合度更加低。**

#### 5、容器启动或停止回调

##### Lifecycle 接口

```java
public interface Lifecycle {
    // 当容器启动时调用
    void start();
    // 当容器停止时调用
    void stop();
    // 当前组件的运行状态
    boolean isRunning();
}
```

编写一个Demo如下：

```java
public class Main {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(LifeCycleConfig.class);
		ac.start();
		ac.stop();
	}
}

@Component
public class LifeCycleService implements Lifecycle {

	boolean isRunning;

	@Override
	public void start() {
		isRunning = true;
		System.out.println("LifeCycleService start");
	}

	@Override
	public void stop() {
		isRunning = false;
		System.out.println("LifeCycleService stop");
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}
}
```

运行上面的代码可以发现程序正常打印启动跟停止的日志，在上面的例子中需要注意的时，一定要在start方法执行时将容器的运行状态`isRunning`置为true，否则`stop`方法不会调用

------

在Spring容器中，当接收到start或stop信号时，容器会将这些传递到所有实现了`Lifecycle`的组件上，在Spring内部是通过`LifecycleProcessor`接口来完成这一功能的。其接口定义如下：

##### LifecycleProcessor

```java
public interface LifecycleProcessor extends Lifecycle {
	// 容器刷新时执行
    void onRefresh();
	// 容器关闭时执行
    void onClose();
}
```

从上面的代码中我们可以知道，`LifecycleProcessor`本身也是`Lifecycle`接口的扩展，它添加了两个额外的方法在容器刷新跟关闭时执行。

我们需要注意以下几点：

1. 当我们实现`Lifecycle`接口时，如果我们想要其start或者stop执行，必须显式的调用容器的`start()`或者`stop()`方法。
2. stop方法不一定能保证在我们之前介绍的销毁方法之前执行

------

当我们在容器中对多个Bean配置了在容器启动或停止时的调用时，那么这些Bean中start方法跟stop方法调用的顺序就很重要了。如果两个Bean之间有明确的依赖关系，比如我们通过`@DepnedsOn`注解，或者`@AutoWired`注解向容器表明了Bean之间的依赖关系，如下：

```java
@Component
@DependsOn("b")
class A{
//	@AutoWired
//   B b;
}

@Component
class B{

}
```

这种情况下，b作为被依赖项，其start方法会在a的start方法前调用，stop方法会在a的stop方法后调用

但是，在某些情况下Bean直接并没有直接的依赖关系，可能我们只知道实现了接口一的所有Bean的方法的优先级要高于实现了接口二的Bean。在这种情况下，我们就需要用到`SmartLifecycle`这个接口了

##### SmartLifecycle

其继承关系如下：

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/20200310232617393.png)

它本身除了继承了`Lifecycle`接口还继承了一个`Phased`接口，其接口定义如下：

```java
public interface Phased { 
    /**    
     * Return the phase value of this object.    
     */
    int getPhase();
}
```

通过上面接口定义的方法，我们可以指定不同Bean方法回调方法执行的优先级。

再来看看`SmartLifecycle`本身这个接口的定义

```java
public interface SmartLifecycle extends Lifecycle, Phased {
    
	int DEFAULT_PHASE = Integer.MAX_VALUE;
	
  // 不需要显示的调用容器的start方法及stop方法也可以执行Bean的start方法跟stop方法
	default boolean isAutoStartup() {
		return true;
	}
	
  // 容器停止时调用的方法
	default void stop(Runnable callback) {
		stop();
		callback.run();
	}
	
  // 优先级，默认最低
	@Override
	default int getPhase() {
		return DEFAULT_PHASE;
	}
}
```

一般情况下，我们并不会复写`isAutoStartup`以及`stop`方法，但是为了指定方法执行的优先级，我们通常会覆盖其中的`getPhase()`方法，默认情况下它的优先级是最低的。我们需要知道的是，**当我们启动容器时，如果有Bean实现了`SmartLifecycle`接口，其`getPhase()`方法返回的值越小，那么对于的start方法执行的时间就会越早，stop方法执行的时机就会越晚。因此，一个实现`SmartLifecycle`的对象，它的`getPhase()`方法返回`Integer.MIN_VALUE`将是第一个执行start方法的Bean和最后一个执行Stop方法的Bean。**

##### 源码分析

源码分析，我们需要分为两个阶段：

###### 启动阶段

整个流程图如下：

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128150556054.png)

我们主要分析的代码在其中的`3-12-2`及`3-12-3`步骤中

`3-12-2`解析，代码如下：

```java
	protected void initLifecycleProcessor() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
    // 判断容器中是否有`lifecycleProcessor`
		if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
			this.lifecycleProcessor =
					beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
			}
		}
    // 如果没有，则new一个DefaultLifecycleProcessor
		else {
			DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
			defaultProcessor.setBeanFactory(beanFactory);
			this.lifecycleProcessor = defaultProcessor;
			beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + LIFECYCLE_PROCESSOR_BEAN_NAME + "' bean, using " +
						"[" + this.lifecycleProcessor.getClass().getSimpleName() + "]");
			}
		}
	}
```

这段代码很简单，就是做了一件事：判断当前容器中是否有一个`lifecycleProcessor`的Bean或者`BeanDefinition`。如果有的话，采用这个提供的`lifecycleProcessor`，如果没有的话自己new一个`DefaultLifecycleProcessor`。这个类主要负载将启动或停止信息传播到具体的Bean当中，我们稍后分析的代码基本都在这个类中。

`3-12-3`解析：

其中的`getLifecycleProcessor()`，就是获取我们上一步提供的`lifecycleProcessor`，然后调用其`onRefresh`方法，代码如下：

```java
public void onRefresh() {
    // 将start信号传递到Bean
    startBeans(true); 
    // 这个类本身也是一个实现了Lifecycle的接口的对象，将其running置为true,标记为运行中
    this.running = true;
}
```

之后调用了`startBeans`方法

```java
private void startBeans(boolean autoStartupOnly) {
    
    // 获取所有实现了Lifecycle接口的Bean,如果采用了factroyBean的方式配置了一个LifecycleBean
    // ,那么factroyBean本身也要实现Lifecycle接口
    // 配置为懒加载的LifecycleBean必须实现SmartLifeCycle才能被调用start方法
    Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
    
    // key:如果实现了SmartLifeCycle，则为其getPhase方法返回的值，如果只是实现了Lifecycle，则返回0
    // value:相同phase的Lifecycle的集合，并将其封装到了一个LifecycleGroup中
    Map<Integer, LifecycleGroup> phases = new HashMap<>();
    
    // 遍历所有的lifecycleBeans，填充上面申明的map
    lifecycleBeans.forEach((beanName, bean) -> {
        
        // 我们可以看到autoStartupOnly这个变量在上层传递过来的
        // 这个参数意味着是否只启动“自动”的Bean,这是什么意思呢？就是说，不需要手动调用容器的start方法
        // 从这里可以看出，实现了SmartLifecycle接口的类并且其isAutoStartup如果返回true的话，会在容器启动过程中自动调用，而仅仅实现了Lifecycle接口的类并不会被调用。
        // 如果我们去阅读容器的start方法的会发现，当调用链到达这个方法时，autoStartupOnly这个变量写死的为false
        if (!autoStartupOnly || (bean instanceof SmartLifecycle && ((SmartLifecycle) bean).isAutoStartup())) {
            
            // 获取这个Bean执行的阶段，实际上就是调用SmartLifecycle中的getPhase方法
            // 如果没有实现SmartLifecycle，而是单纯的实现了Lifecycle，那么直接返回0
            int phase = getPhase(bean);
            
            // 下面就是一个填充Map的操作，有的话add,没有的话直接new一个，比较简单
            LifecycleGroup group = phases.get(phase);
            if (group == null) {
                
                // LifecycleGroup构造函数需要四个参数
                // phase：代表这一组lifecycleBeans的执行阶段
                // timeoutPerShutdownPhase：因为lifecycleBean中的stop方法可以在另一个线程中运行，所以为了确保当前阶段的所有lifecycleBean都执行完，Spring使用了CountDownLatch，而为了防止无休止的等待下去，所有这里设置了一个等待的最大时间，默认为30秒
                // lifecycleBeans：所有的实现了Lifecycle的Bean
                // autoStartupOnly: 手动调用容器的start方法时，为false。容器启动阶段自动调用时为true,详细的含义在上面解释过了
                group = new LifecycleGroup(phase, this.timeoutPerShutdownPhase, lifecycleBeans, autoStartupOnly);
                phases.put(phase, group);
            }
            group.add(beanName, bean);
        }
    });
    if (!phases.isEmpty()) {
        List<Integer> keys = new ArrayList<>(phases.keySet());
        // 升序排序
        Collections.sort(keys);
        for (Integer key : keys) {
            // 获取每个阶段下所有的lifecycleBean，然后调用其start方法
            phases.get(key).start();
        }
    }
}
```

跟踪代码可以发现，start方法最终调用到了`doStart`方法，其代码如下

```java
private void doStart(Map<String, ? extends Lifecycle> lifecycleBeans, String beanName, boolean autoStartupOnly) {
    Lifecycle bean = lifecycleBeans.remove(beanName);
    if (bean != null && bean != this) {
        // 获取这个Bean依赖的其它Bean,在启动时先启动其依赖的Bean
        String[] dependenciesForBean = getBeanFactory().getDependenciesForBean(beanName);
        for (String dependency : dependenciesForBean) {
            doStart(lifecycleBeans, dependency, autoStartupOnly);
        }
        if (!bean.isRunning() &&
            (!autoStartupOnly || !(bean instanceof SmartLifecycle) || ((SmartLifecycle) bean).isAutoStartup())) {
            try {
                bean.start();
            }
            catch (Throwable ex) {
                throw new ApplicationContextException("Failed to start bean '" + beanName + "'", ex);
            }
        }
    }
}
```

上面的逻辑可以归结为一句话：**获取这个Bean依赖的其它Bean,在启动时先启动其依赖的Bean**，这也验证了我们从官网上得出的结论。

###### 停止阶段

停止容器有两种办法，一种时显式的调用容器的stop或者close方法，如下：

```java
public static void main(String[] args) {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
    ac.register(LifeCycleConfig.class);
    ac.refresh();
    ac.stop();
    //		ac.close();
}
```

而另外一个中是注册一个`JVM`退出时的钩子，如下：

```java
public static void main(String[] args) {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
    ac.register(LifeCycleConfig.class);
    // 当main函数运行完成后，会调用容器doClose方法
    ac.registerShutdownHook();
    ac.refresh();
}
```

不论是上面哪一种方法，最终都会调用到`DefaultLifecycleProcessor`的`onClose`方法，代码如下：

```java
public void onClose() {
    // 传递所有的停止信号到Bean
    stopBeans();
    // 跟启动阶段一样，因为它本身是一个实现了Lifecycle接口的Bean，所有需要更改它的运行标志
    this.running = false;
}
```

```java
private void stopBeans() {
    // 获取容器中所有的实现了Lifecycle接口的Bean
    Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
    Map<Integer, LifecycleGroup> phases = new HashMap<>();
    lifecycleBeans.forEach((beanName, bean) -> {
        int shutdownPhase = getPhase(bean);
        LifecycleGroup group = phases.get(shutdownPhase);
        if (group == null) {
            group = new LifecycleGroup(shutdownPhase, this.timeoutPerShutdownPhase, lifecycleBeans, false);
            phases.put(shutdownPhase, group);
        }
        // 同一阶段的Bean放到一起
        group.add(beanName, bean);
    });
    if (!phases.isEmpty()) {
        List<Integer> keys = new ArrayList<>(phases.keySet());
        // 跟start阶段不同的是，这里采用的是降序
        // 也就是阶段越后的Bean,越先stop
        keys.sort(Collections.reverseOrder());
        for (Integer key : keys) {
            phases.get(key).stop();
        }
    }
}
```

```java
public void stop() {
    if (this.members.isEmpty()) {
        return;
    }
    this.members.sort(Collections.reverseOrder());
    
    // 创建了一个CountDownLatch，需要等待的线程数量为当前阶段的所有ifecycleBean的数量
    CountDownLatch latch = new CountDownLatch(this.smartMemberCount);
    
    // stop方法可以异步执行，这里保存的是还没有执行完的lifecycleBean的名称
    Set<String> countDownBeanNames = Collections.synchronizedSet(new LinkedHashSet<>());
    
    // 所有lifecycleBeans的名字集合
    Set<String> lifecycleBeanNames = new HashSet<>(this.lifecycleBeans.keySet());
    for (LifecycleGroupMember member : this.members) {
        if (lifecycleBeanNames.contains(member.name)) {
            doStop(this.lifecycleBeans, member.name, latch, countDownBeanNames);
        }
        else if (member.bean instanceof SmartLifecycle) {
            // 按理说，这段代码永远不会执行，可能是版本遗留的代码没有进行删除
            // 大家可以自行对比4.x的代码跟5.x的代码
            latch.countDown();
        }
    }
    try {
        // 最大等待时间30s，超时进行日志打印
        latch.await(this.timeout, TimeUnit.MILLISECONDS);
        if (latch.getCount() > 0 && !countDownBeanNames.isEmpty() && logger.isInfoEnabled()) {
            logger.info("Failed to shut down " + countDownBeanNames.size() + " bean" +
                        (countDownBeanNames.size() > 1 ? "s" : "") + " with phase value " +
                        this.phase + " within timeout of " + this.timeout + ": " + countDownBeanNames);
        }
    }
    catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
    }
}

```

```java
private void doStop(Map<String, ? extends Lifecycle> lifecycleBeans, final String beanName,
                    final CountDownLatch latch, final Set<String> countDownBeanNames) {
	
    Lifecycle bean = lifecycleBeans.remove(beanName);
    if (bean != null) {
        // 获取这个Bean所被依赖的Bean,先对这些Bean进行stop操作
        String[] dependentBeans = getBeanFactory().getDependentBeans(beanName);
        for (String dependentBean : dependentBeans) {
            doStop(lifecycleBeans, dependentBean, latch, countDownBeanNames);
        }
        try {
            if (bean.isRunning()) {
                if (bean instanceof SmartLifecycle) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Asking bean '" + beanName + "' of type [" +
                                     bean.getClass().getName() + "] to stop");
                    }
                    countDownBeanNames.add(beanName);
                    
                    // 还记得到SmartLifecycle中的stop方法吗？里面接受了一个Runnable参数
                    // 就是在这里地方传进去的。主要就是进行一个操作latch.countDown()，标记当前的lifeCycleBean的stop方法执行完成
                    ((SmartLifecycle) bean).stop(() -> {
                        latch.countDown();
                        countDownBeanNames.remove(beanName);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Bean '" + beanName + "' completed its stop procedure");
                        }
                    });
                }
                else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Stopping bean '" + beanName + "' of type [" +
                                     bean.getClass().getName() + "]");
                    }
                    bean.stop();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully stopped bean '" + beanName + "'");
                    }
                }
            }
            else if (bean instanceof SmartLifecycle) {
                // Don't wait for beans that aren't running...
                latch.countDown();
            }
        }
        catch (Throwable ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to stop bean '" + beanName + "'", ex);
            }
        }
    }
}
```

整个stop方法跟start方法相比，逻辑上并没有很大的区别，除了执行时顺序相反外。

- start方法，先找出这个Bean的所有依赖，然后先启动这个Bean的依赖
- stop方法，先找出哪些Bean依赖了当前的Bean，然后停止这些被依赖的Bean,之后再停止当前的Bean

### Aware接口

在整个Bean的生命周期的初始化阶段，有一个很重要的步骤就是执行相关的Aware接口，而整个Aware接口执行又可以分为两个阶段：

- 第一阶段，执行`BeanXXXAware`接口
- 执行其它Aware接口

至于为什么需要这样分，我们在进行源码分析的时候就明白了

我们可以发现，**所有的Aware接口都是为了能让我们拿到容器中相关的资源**，比如`BeanNameAware`,可以让我们拿到Bean的名称，`ApplicationContextAware` 可以让我们拿到整个容器。但是使用Aware接口也会相应的带来一些弊病，当我们去实现这些接口时，意味着我们的应用程序跟Spring容器发生了强耦合，***`违背了IOC的原则`***。所以一般情况下，并不推荐采用这种方式，除非我们在编写一些整个应用基础的组件。

#### Spring内部提供了如下这些Aware接口

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200310232657477.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

#### 初始化过程源码分析

回顾我们之前的[流程图](https://daimingzhi.blog.csdn.net/article/details/104786530#jump)，我们可以看到，创建Bean的动作主要发生在`3-11-6-4`步骤中，主要分为三步：

1. `createBeanInstance` ,创建实例
2. `populateBean`,属性注入
3. `initializeBean`,初始化

我们今天要分析的代码主要就是第`3-11-6-4-3`步，其完成的功能主要就是初始化，相对于我们之前分析过的代码来说，这段代码算比较简单的：

```java
protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
		if (System.getSecurityManager() != null) {
			AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
				invokeAwareMethods(beanName, bean);
				return null;
			}, getAccessControlContext());
		}
		else {
      // 第一步：执行Aware接口中的方法，需要主要的是，不是所有的Aware接口都是在这步执行了
			invokeAwareMethods(beanName, bean);
		}

		Object wrappedBean = bean;
		if (mbd == null || !mbd.isSynthetic()) {
      // 第二步：完成Aware接口方法的执行,以及@PostConstructor,@PreDestroy注解的处理
			wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
		}

		try {
      // 第三步：完成初始化方法执行
			invokeInitMethods(beanName, wrappedBean, mbd);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					(mbd != null ? mbd.getResourceDescription() : null),
					beanName, "Invocation of init method failed", ex);
		}
		if (mbd == null || !mbd.isSynthetic()) {
      // 第四步：完成AOP代理
			wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
		}

		return wrappedBean;
	}
```

##### 第一步：执行部分aware接口中的方法

```java
private void invokeAwareMethods(final String beanName, final Object bean) {
    if (bean instanceof Aware) {
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
        if (bean instanceof BeanClassLoaderAware) {
            ClassLoader bcl = getBeanClassLoader();
            if (bcl != null) {
                ((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
            }
        }
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
        }
    }
}
```

可以看到，在`invokeAwareMethods`这个方法中，并不是所有的Aware接口都会被执行，只有`BeanNameAware`,`BeanClassLoaderAware`,`BeanFactoryAware`这三个接口会被执行，这也是为什么我单独将`BeanXXXAware`这一类的接口划分为一组的原因。这三个Aware接口分别实现的功能为：

1. `BeanNameAware`：获取Bean的名字
2. `BeanClassLoaderAware`：获取加载这个Bean的类加载器
3. `BeanFactoryAware`：获取当前的BeanFactory

##### 第二步：完成Aware接口方法的执行，以及@PostConstructor,@PreDestroy注解的处理

- Aware接口执行，除了我们上面介绍的三个Aware接口，其余的接口都会在这个阶段执行，例如我们之前说到的`ApplicationContextAware` 接口，它会被一个专门的后置处理器`ApplicationContextAwareProcessor`处理。其余的接口也是类似的操作，这里就不在赘述了
- `@PostConstructor`,`@PreDestroy`两个注解的处理。这两个注解会被`CommonAnnotationBeanPostProcessor`这个后置处理器处理，需要注意的是`@Resource`注解也是被这个后置处理器进行处理的。关于注解的处理逻辑，我们后面的源码阅读相关文章中再做详细分析。

##### 第三步：完成初始化方法执行

```java
protected void invokeInitMethods(String beanName, final Object bean, @Nullable RootBeanDefinition mbd) throws Throwable {
	  // 是否实现了 InitializingBean接口
    boolean isInitializingBean = (bean instanceof InitializingBean);
    if (isInitializingBean && (mbd == null || 
                              // 这个判断基本恒成立，除非手动改变了BD的属性
                              !mbd.isExternallyManagedInitMethod("afterPropertiesSet")){
        if (logger.isTraceEnabled()) {
            logger.trace("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
        }
        if (System.getSecurityManager() != null) {
            try {
                AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                   // 调用afterPropertiesSet方法
                    ((InitializingBean) bean).afterPropertiesSet();
                    return null;
                }, getAccessControlContext());
            }
            catch (PrivilegedActionException pae) {
                throw pae.getException();
            }
        }
        else {
              // 调用afterPropertiesSet方法
            ((InitializingBean) bean).afterPropertiesSet();
        }
    }
	
    if (mbd != null && bean.getClass() != NullBean.class) {
        String initMethodName = mbd.getInitMethodName();
        if (StringUtils.hasLength(initMethodName) &&
            !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
            !mbd.isExternallyManagedInitMethod(initMethodName)) {
            invokeCustomInitMethod(beanName, bean, mbd);
        }
    }
}
```

整段代码的逻辑还是很简单的，先判断是否实现了对应的生命周期回调的接口（`InitializingBean`），如果实现了接口，先调用接口中的`afterPropertiesSet`方法。之后在判断是否提供了`initMethod`，也就是在XML中的Bean标签中提供了`init-method`属性。

##### 第四步：完成AOP代理

`AOP`代理实现的具体过程放到之后的文章中分析，我们暂时只需要知道`AOP`是在Bean完成了所有初始化方法后完成的即可。这也不难理解，在进行`AOP`之前必须保证我们的Bean已经被充分的”装配“了。

### 总结

就目前而言，我们可以将整个Bean的生命周期总结如下：

![image-20210128151819754](https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210128151819754.png)
在上图中，实例话跟属性注入的过程我们还没有分析，在后续的文章中，我们将对其进行详细的分析。销毁阶段并不复杂，所以这里也不做分析了，直接给出结论，大概可以自己阅读代码，入口在容器的`close`方法中。

另外，我这里并没有将实现了`LifeCycle`接口的Bean中的start方法跟stop方法算入到整个Bean的生命周期中，大家只要知道，如果实现了`SmartLifeCyle`接口，那么在容器启动时也会默认调用其`start`方法，并且调用的时机在Bean完成初始化后，而stop方法将在Bean销毁前调用。

## Spring官网阅读（十）Spring中Bean的生命周期（下）

> 在上篇文章中，我们已经对Bean的生命周期做了简单的介绍，主要介绍了整个生命周期中的初始化阶段以及基于容器启动停止时`LifeCycleBean`的回调机制，另外对Bean的销毁过程也做了简单介绍。但是对于整个Bean的生命周期，这还只是一小部分，在这篇文章中，我们将学习完成剩下部分的学习，同时对之前的内容做一次复习。整个Bean的生命周期，按照我们之前的介绍，可以分为四部分
>
> - 实例化
> - 属性注入
> - 初始化
> - 销毁
>
> 本文主要介绍实例化及属性注入阶段

### 生命周期概念补充

虽然我们一直说整个Bean的生命周期分为四个部分，但是相信很多同学一直对Bean的生命周期到底从哪里开始，到哪里结束没有一个清晰的概念。可能你会说，不就是从**实例化**开始，到**销毁**结束吗？当然，这并没有错，但是具体什么时候算开始实例化呢？什么时候又算销毁呢？这个问题你是否能清楚的回答呢？如果不能，请继续往下看。

笔者认为，整个Spring中Bean的生命周期，**从第一次调用后置处理器中的`applyBeanPostProcessorsBeforeInstantiation`方法开始的**，这个方法见名知意，翻译过来就是**在实例化之前调用后置处理器**。**而`applyBeanPostProcessorsAfterInitialization`方法的调用，意味着Bean的生命周期中创建阶段的结束**。对于销毁没有什么歧义，就是在调用对应Bean的销毁方法就以为着这个Bean走到了生命的尽头，标志着Bean生命周期的结束。那么结合上篇文章中的结论，我现在把Bean的生命周期的范围界定如下：

![image-20210128152405056](https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210128152405056.png)

需要注意的是，对于`BeanDefinion`的扫描，解析，验证并不属于Bean的生命周期的一部分。这样清晰的界定Bean的生命周期的概念是很有必要的，也许刚刚开始对于我们而言，Bean的生命周期就是一团乱麻，但是至少现在我们已经抓住了**线头**。而整个Bean的生命周期，我将其分为了两部分

- 创建
- 销毁

对于销毁阶段，我们不需要过多关注，对于创建阶段，

开始的标志行为为：`applyBeanPostProcessorsBeforeInstantiation`方法执行，

结束的标志行为为：`applyBeanPostProcessorsAfterInitialization`方法执行。

基于上面的结论，我们开始进行接下来的分析对于本文中代码的分析，我们还是参照下面这个图

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128152928172.png)

### 实例化

整个实例化的过程主要对于上图中的`3-11-6-4`（`createBean`）以及`3-11-6-4-1`（`doCreateBean`）步骤

#### createBean流程分析

代码如下：

```java
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {

		RootBeanDefinition mbdToUse = mbd;
		
    // 第一步：解析BeanDefinition中的beanClass属性
		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
			mbdToUse = new RootBeanDefinition(mbd);
			mbdToUse.setBeanClass(resolvedClass);
		}
    
		try {
      // 第二步：处理lookup-method跟replace-method，判断是否存在方法的重载
			mbdToUse.prepareMethodOverrides();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
					beanName, "Validation of method overrides failed", ex);
		}

		try {
			// 第三步：判断这个类在之后是否需要进行AOP代理
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
			if (bean != null) {
				return bean;
			}
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
					"BeanPostProcessor before instantiation of bean failed", ex);
		}

		try {
      // 开始创建Bean
			Object beanInstance = doCreateBean(beanName, mbdToUse, args);
			return beanInstance;
		}
		catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
		}
	}
```

可以看到，`第一步`跟`第二步`还是在对`BeanDefinition`中的一些属性做处理，它并不属于我们Bean的生命周期的一部分，我们直接跳过，接下来看第三步的代码：

```java
protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
    Object bean = null;
    if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
        // 不是合成类，并且有实例化后置处理器。这个判断基本上恒成立
        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            // 获取这个BeanDefinition的类型
            Class<?> targetType = determineTargetType(beanName, mbd);
            if (targetType != null) {
                // 这里执行的主要是AbstractAutoProxyCreator这个类中的方法，决定是否要进行AOP代理
                bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                // 这里执行了一个短路操作，如果在这个后置处理中直接返回了一个Bean,那么后面相关的操作就不会执行了，只会执行一个AOP的代理操作
                if (bean != null) {
                    // 虽然这个Bean被短路了，意味着不需要经过后面的初始化阶段，但是如果需要代理的话，还是要进行AOP代理，这个地方的短路操作只是意味着我们直接在后置处理器中提供了一个准备充分的的Bean，这个Bean不需要进行初始化，但需不需要进行代理，任然由AbstractAutoProxyCreator的applyBeanPostProcessorsBeforeInstantiation方法决定。在这个地方还是要调用一次Bean的初始化后置处理器保证Bean被完全的处理完
                    bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                }
            }
        }
        // bean != null基本会一直返回false,所以beforeInstantiationResolved这个变量也会一直为false
        mbd.beforeInstantiationResolved = (bean != null);
    }
    return bean;
}
```

对于`AbstractAutoProxyCreator`中`applyBeanPostProcessorsBeforeInstantiation`这个方法的分析我们暂且不管，等到`AOP`学习阶段在进行详细分析。我们暂且只需要知道**这个方法会决定在后续中要不要为这个Bean产生代理对象**。

#### doCreateBean流程分析

```java
	protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
			throws BeanCreationException {

		BeanWrapper instanceWrapper = null;
		if (mbd.isSingleton()) {
       // 第一步：单例情况下，看factoryBeanInstanceCache这个缓存中是否有
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}
		if (instanceWrapper == null) {
      // 第二步：这里创建对象
			instanceWrapper = createBeanInstance(beanName, mbd, args);
		}
		final Object bean = instanceWrapper.getWrappedInstance();
		Class<?> beanType = instanceWrapper.getWrappedClass();
		if (beanType != NullBean.class) {
			mbd.resolvedTargetType = beanType;
		}

		synchronized (mbd.postProcessingLock) {
			if (!mbd.postProcessed) {
				try {
          // 第三步：后置处理器处理
					applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				}
				catch (Throwable ex) {
					// 省略异常处理
				}
				mbd.postProcessed = true;
			}
		}
		
    // 循环引用相关，源码阅读阶段再来解读这段代码，暂且就关注以下后置处理器的调用时机
		boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
				isSingletonCurrentlyInCreation(beanName));
		if (earlySingletonExposure) {
			// 第四步：调用后置处理器，早期曝光一个工厂对象
			addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
		}

		Object exposedObject = bean;
		try {
      // 第五步：属性注入
			populateBean(beanName, mbd, instanceWrapper);
      // 第六步：初始化
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
		catch (Throwable ex) {
			if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
				throw (BeanCreationException) ex;
			}
			else {
				// 省略异常处理
			}
		}

		if (earlySingletonExposure) {
			Object earlySingletonReference = getSingleton(beanName, false);
			if (earlySingletonReference != null) {
				if (exposedObject == bean) {
					exposedObject = earlySingletonReference;
				}
				else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
					String[] dependentBeans = getDependentBeans(beanName);
					Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
					for (String dependentBean : dependentBeans) {
						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
							actualDependentBeans.add(dependentBean);
						}
					}
					if (!actualDependentBeans.isEmpty()) {
						// 省略异常处理
					}
				}
			}
		}

		try {
      // 第七步：注册需要销毁的Bean,放到一个需要销毁的Map中（disposableBeans）
			registerDisposableBeanIfNecessary(beanName, bean, mbd);
		}
		catch (BeanDefinitionValidationException ex) {
			// 省略异常处理
		}

		return exposedObject;
	}
```

##### 第一步：factoryBeanInstanceCache什么时候不为空？

```java
if (mbd.isSingleton()) {
    // 第一步：单例情况下，看factoryBeanInstanceCache这个缓存中是否有
    instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
}
```

这段代码深究起来很复杂，我这里就单纯用理论解释下：

假设我们现在有一个`IndexService`，它有一个属性A，代码如下：

```java
@Component
public class IndexService {
	@Autowired
	A a;

	public A getA() {
		return a;
	}
}
```

而这个A又是采用`FactroyBean`的形式配置的，如下：

```java
@Component
public class MyFactoryBean implements SmartFactoryBean {
	@Override
	public A getObject() throws Exception {
		return new A();
	}
	@Override
	public Class<?> getObjectType() {
		return A.class;
	}
    // 这个地方并不一定要配置成懒加载，这里只是为了让MyFactoryBean这个Bean在IndexService之后实例化
	@Override
	public boolean isEagerInit() {
		return false;
	}
}
```

我们思考一个问题，在上面这种场景下，当`IndexService`要完成属性注入时，Spring会怎么做呢？

Spring现在知道`IndexService`要注入一个类型为A的属性，所以它会遍历所有的解析出来的`BeanDefinition`，然后每一个`BeanDefinition`中的类型是不是A类型，类似下面这样：

```java
for (String beanName : this.beanDefinitionNames) {
	// 1.获取BeanDefinition
    // 2.根据BeanDefinition中的定义判断是否是一个A
}
```

上面这种判断大部分情况下是成立的，但是对于一种特殊的Bean是不行的，就是我们之前介绍过的`FactoryBean`，因为我们配置`FactoacryBean`的目的并不是直接使用`FactoryBean`这个Bean自身，而是想要通过它的`getObject`方法将一个对象放到Spring容器中，所以当我们遍历到一个`BeanDefinition`，并且这个`BeanDefinition`是一个`FactoacryBean`时就需要做特殊处理，我们知道`FactoacryBean`中有一个`getObjectType`方法，通过这个方法我们可以得到要被这个`FactoacryBean`创建的对象的类型，如果我们能调用这个方法的话，那么我们就可以来判断这个类型是不是一个A了。

但是，在我们上面的例子中，这个时候`MyFactoryBean`还没有被创建出来。所以Spring在这个时候会去实例化这个`MyFactoryBean`，然后调用其`getObjectType`方法，再做类型判断，最后进行属性注入，伪代码如下：

```java
for (String beanName : this.beanDefinitionNames) {
	// 1.获取BeanDefinition
    // 2.如果不是一个FactoacryBean，直接根据BeanDefinition中的属性判断
    if(不是一个FactoacryBean){
        //直接根据BeanDefinition中的属性判断是不是A
    }
    // 3.如果是一个FactoacryBean
    if(是一个FactoacryBean){
        // 先创建这个FactoacryBean，然后再调用getObjectType方法了
    }
}
```

大家可以根据我们上面的代码自己调试，我这里就不放debug的截图了。

##### 第二步：创建对象（createBeanInstance）

```java
protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
		// 获取到解析后的beanClass
		Class<?> beanClass = resolveBeanClass(mbd, beanName);
		
    // 忽略异常处理
		Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
		if (instanceSupplier != null) {
			return obtainFromSupplier(instanceSupplier, beanName);
		}
		
    // 获取工厂方法，用于之后创建对象 
		if (mbd.getFactoryMethodName() != null) {
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}

		// 原型情况下避免多次解析
		boolean resolved = false;
		boolean autowireNecessary = false;
		if (args == null) {
			synchronized (mbd.constructorArgumentLock) {
				if (mbd.resolvedConstructorOrFactoryMethod != null) {
					resolved = true;
					autowireNecessary = mbd.constructorArgumentsResolved;
				}
			}
		}
		if (resolved) {
			if (autowireNecessary) {
				return autowireConstructor(beanName, mbd, null, null);
			}
			else {
				return instantiateBean(beanName, mbd);
			}
		}

		// 跟后置处理器相关，我们主要关注这行代码
		Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
		if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
				mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
			return autowireConstructor(beanName, mbd, ctors, args);
		}

		// Preferred constructors for default construction?
		ctors = mbd.getPreferredConstructors();
		if (ctors != null) {
			return autowireConstructor(beanName, mbd, ctors, null);
		}

		// 默认使用无参构造函数创建对象
		return instantiateBean(beanName, mbd);
	}
```

在创建对象的时候，其余的代码我们暂时不做过多关注。我们暂且知道在创建对象的过程中，Spring会调用一个后置处理器来推断构造函数。

##### 第三步：applyMergedBeanDefinitionPostProcessors

应用合并后的`BeanDefinition`，Spring自身利用这点做了一些注解元数据的缓存。

我们就以`AutowiredAnnotationBeanPostProcessor`这个类的对应方法看一下其大概作用

```java
public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
    // 这个方法就是找到这个正在创建的Bean中需要注入的字段，并放入缓存中
    InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null);
    metadata.checkConfigMembers(beanDefinition);
}
```

##### 第四步：getEarlyBeanReference

```java
protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
    Object exposedObject = bean;
    if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                // 在这里保证注入的对象是一个代理的对象（如果需要代理的话），主要用于循环依赖
                exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
            }
        }
    }
    return exposedObject;
}
```

属性注入

##### 第五步：属性注入（populateBean）

```java
	protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
		if (bw == null) {
			if (mbd.hasPropertyValues()) {
				// 省略异常
			}
			else {
				return;
			}
		}

		boolean continueWithPropertyPopulation = true;

		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
					if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {			// 主要判断之后是否需要进行属性注入
						continueWithPropertyPopulation = false;
						break;
					}
				}
			}
		}

		if (!continueWithPropertyPopulation) {
			return;
		}

		PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);
		
        // 自动注入模型下，找到合适的属性，在后续方法中再进行注入
		if (mbd.getResolvedAutowireMode() == AUTOWIRE_BY_NAME || mbd.getResolvedAutowireMode() == AUTOWIRE_BY_TYPE) {
			MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
			// Add property values based on autowire by name if applicable.
			if (mbd.getResolvedAutowireMode() == AUTOWIRE_BY_NAME) {
				autowireByName(beanName, mbd, bw, newPvs);
			}
			// Add property values based on autowire by type if applicable.
			if (mbd.getResolvedAutowireMode() == AUTOWIRE_BY_TYPE) {
				autowireByType(beanName, mbd, bw, newPvs);
			}
			pvs = newPvs;
		}
		
		boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
		boolean needsDepCheck = (mbd.getDependencyCheck() != AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);

		PropertyDescriptor[] filteredPds = null;
		if (hasInstAwareBpps) {
			if (pvs == null) {
				pvs = mbd.getPropertyValues();
			}
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                    // 精确注入下，在这里完成属性注入
					PropertyValues pvsToUse = ibp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
                    // 一般不会进行这个方法
					if (pvsToUse == null) {
						if (filteredPds == null) {
							filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
						}
						pvsToUse = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
						if (pvsToUse == null) {
							return;
						}
					}
					pvs = pvsToUse;
				}
			}
		}
		if (needsDepCheck) {
			if (filteredPds == null) {
				filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
			}
			checkDependencies(beanName, mbd, filteredPds, pvs);
		}

		if (pvs != null) {
            // XML配置，或者自动注入，会将之前找到的属性在这里进行注入
			applyPropertyValues(beanName, mbd, bw, pvs);
		}
	}
```

在上面整个流程中，我们主要关注一个方法，`postProcessProperties`，这个方法会将之前通过`postProcessMergedBeanDefinition`方法找到的注入点，在这一步进行注入。完成属性注入后，就开始初始化了，初始化的流程在上篇文章中已经介绍过了，这里就不再赘述了。

### 总结

在这两篇文章中，我们已经对Bean的全部的生命周期做了详细分析，当然，对于一些复杂的代码，暂时还没有去深究，因为之后打算写一系列专门的源码分析文章。大家可以关注我后续的文章。对于整个Bean的生命周期可以总结画图如下

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128153713040.png)

首先，整个Bean的生命周期我们将其划分为两个部分

1. 创建
2. 销毁

对于创建阶段，我们又将其分为三步

- 实例化
- 属性注入
- 初始化

我们可以看到，在整个过程中`BeanPostPorcessor`穿插执行，辅助Spring完成了整个Bean的生命周期。

## Spring官网阅读（十一）ApplicationContext详细介绍（上）

> 在前面的文章中，我们已经完成了官网中关于`IOC`内容核心的部分。包括容器的概念，Spring创建Bean的模型`BeanDefinition`的介绍，容器的扩展点（`BeanFactoryPostProcessor`，`FactroyBean`，`BeanPostProcessor`）以及最重要的Bean的生命周期等。接下来大概还要花三篇文章完成对官网中第一大节的其它内容的学习，之所以要这么做，是笔者自己粗读了一篇源码后，再读一遍官网，发现源码中的很多细节以及难点都在官网中介绍了。所以这里先跟大家一起把官网中的内容都过一遍，也是为了更好的进入源码学习阶段。
>
> 本文主要涉及到官网中的[1.13](https://docs.spring.io/spring/docs/5.1.13.BUILD-SNAPSHOT/spring-framework-reference/core.html#beans-environment),[1.15](https://docs.spring.io/spring/docs/5.1.13.BUILD-SNAPSHOT/spring-framework-reference/core.html#context-introduction),[1.16](https://docs.spring.io/spring/docs/5.1.13.BUILD-SNAPSHOT/spring-framework-reference/core.html#beans-beanfactory)小节中的内容以及[第二大节](https://docs.spring.io/spring/docs/5.1.13.BUILD-SNAPSHOT/spring-framework-reference/core.html#resources)的内容

### ApplicationContext

#### 1、ApplicationContext的继承关系

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200316003828654.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

从上图中可以发现，`ApplicationContext`接口继承了很多接口，这些接口我们可以将其分为五类：

- `MessageSource`，主要用于国际化
- `ApplicationEventPublisher`，提供了事件发布功能
- `EnvironmentCapable`，可以获取容器当前运行的环境
- `ResourceLoader`，主要用于加载资源文件
- `BeanFactory`，负责配置、创建、管理Bean，`IOC`功能的实现主要就依赖于该接口子类实现。

关于这些的接口的具体功能的介绍在后文会介绍，当前我们需要知道最重要的一点就是`ApplicationContext`继承了`BeanFactory`接口，也就是说它具有`BeanFactory`所有的功能。

#### 2、ApplicationContext的功能

### Spring中的国际化（MessageSource）

#### 国际化是什么？

应用程序运行时，可根据客户端操作系統的国家/地区、语言的不同而显示不同的界面，比如客户端OS的语言环境为大陆的简体中文，程序就显示为简体中文，客户端OS的语言环境为美国——英语，程序就显示美式英语。OS的语言环境可在控制面板中手动设置。国际化的英文单词是Internationalization，单词较长，通常简称`i18n`，I是第一个字母，18表示中间省略了18个字母，N是最后一个字母。

假设我们正在开发一个支持多国语言的Web应用程序，要求系统能够根据客户端的系统的语言类型返回对应的界面：英文的操作系统返回英文界面，而中文的操作系统则返回中文界面——这便是典型的`i18n`国际化问题。对于有国际化要求的应用系统，我们不能简单地采用硬编码的方式编写用户界面信息、报错信息等内容，而必须为这些需要国际化的信息进行特殊处理。简单来说，就是**为每种语言提供一套相应的资源文件，并以规范化命名的方式保存在特定的目录中，由系统自动根据客户端语言选择适合的资源文件**。

#### JAVA中的国际化

**国际化信息**也称为**本地化信息**，一般需要两个条件才可以确定一个特定类型的本地化信息，它们分别是“*语言类型*”和“*国家/地区的类型*”。如中文本地化信息既有中国大陆地区的中文，又有中国台湾、中国香港地区的中文，还有新加坡地区的中文。

【部分国际化代码】：

```
ar_sa 阿拉伯语(沙特阿拉伯)
ar_iq 阿拉伯语(伊拉克) 
eu 巴斯克语
bg 保加利亚语 
zh_tw 中文(中国台湾)
zh_cn 中文(中华人民共和国)
zh_hk 中文(中国香港特别行政区)
zh_sg 中文(新加坡)
hr 克罗地亚语 
en 英语
en_us 英语(美国)
en_gb 英语(英国)
en_au 英语(澳大利亚)
en_ca 英语(加拿大)
```

##### 本地化对象（Locale）

Java通过`java.util.Locale`类表示一个本地化对象，它允许通过语言参数和国家/地区参数创建一个确定的本地化对象。

```java
Locale locale=new Locale("zh","cn");//中文，中国
Locale locale2=new Locale("en","us");//英文，美国
Locale locale3=new Locale("zh");//中文--不指定国家
Locale locale4=Locale.CHINA;//中文，中国
Locale locale5=Locale.CHINESE;//中文
```

在持有一个`Locale`对象后，我们需要将同一个文字或者数字根据不同的地区/语言格式化成不同的表现形式，所以这里我们还需要一个格式化的操作，`JDK`给我们提供以下几个常见的类用于国际化格式化

`NumberFormat`：可以处理数字，百分数，货币等。下面以货币为例：

```java
public static void main(String[] args) {
    // 1.通过语言跟地区确定一个Locale对象
    // 中国，中文
    Locale chinaLocale = new Locale("zh", "cn");
    // 美国，英文
    Locale usLocale = new Locale("en", "us");
    // 获取货币格式化对象
    NumberFormat chinaCurrencyFormat = NumberFormat.getCurrencyInstance(chinaLocale);
    NumberFormat usLocaleCurrencyFormat = NumberFormat.getCurrencyInstance(usLocale);
    // 中文，中国下的货币表现形式
    String chinaCurrency = chinaCurrencyFormat.format(99.9);  // 输出 ￥99.90
    // 美国，英文下的货币表现形式
    String usCurrency = usLocaleCurrencyFormat.format(99.9); // 输出 $99.90
    System.out.println(chinaCurrency);
    System.out.println(usCurrency);
}
```

##### 格式化对象

`DateFormat`：通过`DateFormat#getDateInstance(int style,Locale locale)`方法按本地化的方式对日期进行格式化操作。该方法第一个入参为时间样式，第二个入参为本地化对象

```java
public static void main(String[] args) {
    // 1.通过语言跟地区确定一个Locale对象
    // 中国，中文
    Locale chinaLocale = new Locale("zh", "cn");
    // 美国，英文
    Locale usLocale = new Locale("en", "us");
    DateFormat chinaDateFormat = DateFormat.getDateInstance(DateFormat.YEAR_FIELD,chinaLocale);
    DateFormat usDateFormat = DateFormat.getDateInstance(DateFormat.YEAR_FIELD,usLocale);
    System.out.println(chinaDateFormat.format(new Date())); // 输出 2020年1月15日
    System.out.println(usDateFormat.format(new Date()));    // 输出 January 15, 2020
}
```

`MessageFormat`：在`NumberFormat`和`DateFormat`的基础上提供了强大的占位符字符串的格式化功能，它支持时间、货币、数字以及对象属性的格式化操作

1. 简单的占位符替换

```java
public static void main(String[] args) {
    // 1.通过语言跟地区确定一个Locale对象
    // 中国，中文
    Locale chinaLocale = new Locale("zh", "cn");
    String str1 = "{0}，你好！你于{1}在农业银行存入{2}。";
    MessageFormat messageFormat = new MessageFormat(str1,chinaLocale);
    Object[] o = {"小红", new Date(), 99.99};
    System.out.println(messageFormat.format(o));
    // 输出：小红，你好！你于20-1-15 下午4:05在农业银行存入99.99。
}
```

1. 指定格式化类型跟格式化样式的占位符替换

```JAVA
public static void main(String[] args) {
    String str1 = "{0}，你好！你于{1,date,long}在农业银行存入{2,number, currency}。";
    MessageFormat messageFormat = new MessageFormat(str1,Locale.CHINA);
    Object[] o = {"小红", new Date(), 1313};
    System.out.println(messageFormat.format(o));
    // 输出：小红，你好！你于2020年1月15日在农业银行存入￥1,313.00。
}
1234567
```

在上面的例子中，0，1，2代表的是占位符的索引，从0开始计数。`date`，`number`为格式化的类型。`long`，`currency`为格式化样式。

- **`FormatType`**：格式化类型，取值范围如下：

 number：调用`NumberFormat`进行格式化

 date：调用`DateFormat`进行格式化

 time：调用`DateFormat`进行格式化

 choice：调用`ChoiceFormat`进行格式化

- **`FormatStyle`**:：设置使用的格式化样式

  short
  medium
  long
  full
  integer
  currency
  percent
  SubformatPattern (子格式模式，形如#.##)

对于具体的使用方法就不多赘述了，大家可以自行百度。

##### 资源文件的加载

在实现国际化的过程中，由于我们的用户界面信息、报错信息等内容都不能采用硬编码的方式，所以为了在不同的区域/语言环境下能进行不同的显示，我们需要为不同的环境提供不同的资源文件，同时需要遵循一定的规范。

- 命名规范：`资源名_语言代码_国/地区代码.properties`

举一个例子：假设资源名为content，语言为英文，国家为美国，则与其对应的本地化资源文件命名为content_en_US.properties。

下面以IDEA为例，创建资源文件并加载读取

1. 创建资源文件，在Resource目录下，创建一个Bundle

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200316003847707.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

1. 添加需要兼容的区域/语言，我这里就添加一个英语/美国，给这个Bundle命名为`i18n`，名字随意

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200316003902465.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

1. 此时会在Resource目录下生成如下的目录结构

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200316003912513.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

在两个配置文件中，我分别添加了两段配置：

【`i18n.properties`】:

```properties
#小明(资源文件对文件内容有严格的要求：只能包含ASCII字符，所以必须将非ASCII字符的内容转换为Unicode代码的表示方式)
name=\u5c0f\u660e
#他十九岁了
age=19
```

【`i18n_en_US.properties`】:

```properties
name=Xiaoming
age=19
```

示例代码：

```java
public static void main(String[] args) {
    // i18n要跟我们之前创建的Bundle的名称一致
    // Locale.US指定了我们要拿这个Bundle下的哪个区域/语言对于的资源文件，这里获取到的是i18n_en_US.properties这个配置文件
    ResourceBundle usResourceBundle = ResourceBundle.getBundle("i18n", Locale.US);
    System.out.println(usResourceBundle.getString("name")); // 输出Xiaoming
    System.out.println(usResourceBundle.getString("age"));
    ResourceBundle chinaResourceBundle = ResourceBundle.getBundle("i18n");
    System.out.println(chinaResourceBundle.getString("name"));  // 输出小明
    System.out.println(chinaResourceBundle.getString("age"));
}
```

#### Spring中的MessageSource

在聊完了JAVA中的国际化后，我们回归主题，`ApplicationContext`接口继承了`MessageSource`接口，`MessageSource`接口又提供了国际化的功能，所以`ApplicationContext`也具有国际化的功能。接下来我们着重看看`MessageSource`这个接口。

##### 接口定义

```java
public interface MessageSource {

     //code表示国际化资源中的属性名；args用于传递格式化串占位符所用的运行期参数；
     //当在资源找不到对应属性名时，返回defaultMessage参数所指定的默认信息；
     //locale表示本地化对象；
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    //与上面的方法类似，只不过在找不到资源中对应的属性名时，
    //直接抛出NoSuchMessageException异常；
    String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

    //将属性名、参数数组以及默认信息封装起来，它的功能和第一个接口方法相同。
    String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

}
123456789101112131415
```

##### UML类图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200316003924136.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

我们依次分析下各个类的作用

1. `HierarchicalMessageSource`，该接口提供了设置获取父容器的方法，用于构建`MessageSource`体系的父子层级结构。其方法定义如下:

```java
// 为当前MessageSource设置父MessageSource
void setParentMessageSource(@Nullable MessageSource parent);

// 获取当前MessageSource的父MessageSource
@Nullable
MessageSource getParentMessageSource();
123456
```

1. `MessageSourceSupport`，这个类的作用类似于我们之前介绍的[MessageFormat](https://daimingzhi.blog.csdn.net/article/details/104890350#format)，主要提供了对消息的格式化功能。从这个继承关系中我们也能看出，Spring在设计时将消息的获取以及格式化进行了分隔。而在我们实际使用到具体的实现类时，又将功能做了聚合。
2. `DelegatingMessageSource`，将所有获取消息的请求委托给父类查找，如果父类没有就报错
3. `AbstractMessageSource`，实现了`HierarchicalMessageSource`，提供了对消息的通用处理方式，方便子类对具体的消息类型实现特定的策略
4. `AbstractResourceBasedMessageSource`，提供了对Bundle的处理方式
5. `ResourceBundleMessageSource`，基于`JDK`的`ResourceBundle`实现，可以根据名称加载Bundle
6. `ReloadableResourceBundleMessageSource`，提供了定时刷新功能，允许在不重启系统的情况下，更新资源的信息。
7. `StaticMessageSource`，主要用于程序测试

##### Spring中的简单使用

我这里直接取官网中的Demo，先看官网上的一段说明：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200316003935536.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

从上文中，我们可以得出以下几点信息：

1. Spring容器在启动时会自动查找一个名称定义的`messageSource`的Bean（同时需要实现`MessageSource`接口），如果找到了，那么所有获取信息的请求都会由这个类处理。如果在当前容器中没有找到的话，会在父容器中继续查找。
2. 如果没有找到，那么Spring会自己new一个`DelegatingMessageSource`对象，并用这个对象处理消息

基于上面的结论，我们可以做如下配置：

```xml
<!--application.xml-->
<bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
    <property name="basenames">
        <list>
            <value>format</value>
            <value>exceptions</value>
            <value>windows</value>
        </list>
    </property>
</bean>
```

同时配置下面三个properties文件：

```properties
# in format.properties
message=Alligators rock!
# in exceptions.properties
argument.required=The {0} argument is required.
```

测试代码：

```java
public static void main(String[] args) {
    MessageSource resources = new ClassPathXmlApplicationContext("application.xml");
    String message1 = resources.getMessage("message", null, "Default", null);
    String message2 = resources.getMessage("argument.required",
                                           new Object [] {"userDao"}, "Required", null);
    System.out.println(message1); // 输出 Alligators rock!
    System.out.println(message2); // 输出 The userDao argument is required.
}
```

同时Spring对资源的加载也遵循我们在JAVA国际化中提到的规范，我们可以将上面例子中的`exceptions.properties`，更名为`exceptions_en_GB.properties`。

```JAVA
// 可以看出这种方式跟我们使用JAVA直接加载国际化资源没有太大差别
public static void main(final String[] args) {
    MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
    String message = resources.getMessage("argument.required",
        new Object [] {"userDao"}, "Required", Locale.UK);
    System.out.println(message);
}
```

### Spring中的环境（Environment）

> 这小结内容对应官网中的[1.13](https://docs.spring.io/spring/docs/5.1.13.BUILD-SNAPSHOT/spring-framework-reference/core.html#beans-environment)小节

在前面的[ApplicationContext的继承关系](https://daimingzhi.blog.csdn.net/article/details/104890350#extends)中我们知道`ApplicationContext`这个接口继承了一个`EnvironmentCapable`接口，而这个接口的定义非常简单，如下

```java
public interface EnvironmentCapable {
	Environment getEnvironment();
}
```

可以看到它只是简单的提供了一个获取`Environment`对象的方法，那么这个`Environment`对象是做什么的呢？

##### 1、什么是环境（Environment）？

它其实代表了当前Spring容器的运行环境，比如`JDK`环境，系统环境；每个环境都有自己的配置数据，如`System.getProperties()`可以拿到`JDK`环境数据、`System.getenv()`可以拿到系统变量，`ServletContext.getInitParameter()`可以拿到`Servlet`环境配置数据。Spring抽象了一个Environment来表示Spring应用程序环境配置，它整合了各种各样的外部环境，并且提供统一访问的方法。

##### 2、接口定义

```java
public interface Environment extends PropertyResolver {
    
  // 获取当前激活的Profile的名称
	String[] getActiveProfiles();
	
  // 获取默认的Profile的名称
	String[] getDefaultProfiles();

	@Deprecated
	boolean acceptsProfiles(String... profiles);
	
  // 判断指定的profiles是否被激活
	boolean acceptsProfiles(Profiles profiles);

}

public interface PropertyResolver {
	// 当前的环境中是否包含这个属性
	boolean containsProperty(String key);
	
  //获取属性值 如果找不到返回null   
	@Nullable
	String getProperty(String key);
	
  // 获取属性值，如果找不到返回默认值        
	String getProperty(String key, String defaultValue);
	
  // 获取指定类型的属性值，找不到返回null  
	@Nullable
	<T> T getProperty(String key, Class<T> targetType);
	
  // 获取指定类型的属性值，找不到返回默认值  
	<T> T getProperty(String key, Class<T> targetType, T defaultValue);
	
  // 获取属性值，找不到抛出异常IllegalStateException  
	String getRequiredProperty(String key) throws IllegalStateException;
	
  // 获取指定类型的属性值，找不到抛出异常IllegalStateException         
	<T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;
	
  // 替换文本中的占位符（${key}）到属性值，找不到不解析  
	String resolvePlaceholders(String text);
    
  // 替换文本中的占位符（${key}）到属性值，找不到抛出异常IllegalArgumentException 
	String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
```

我们可以看到，`Environment`接口继承了`PropertyResolver`，而`Environment`接口自身主要提供了对`Profile`的操作，`PropertyResolver`接口中主要提供了对当前运行环境中属性的操作，如果我们去查看它的一些方法的实现可以发现，对属性的操作大都依赖于`PropertySource`。

所以在对`Environment`接口学习前，我们先要了解`Profile`以及`PropertySource`

##### 3、Profile

我们先看官网上对Profile的介绍：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200316003947745.png)

从上面这段话中我们可以知道

1. Profile是一组逻辑上的Bean的定义
2. 只有这个Profile被激活时，这组Bean才会被注册到容器中
3. 我们既可以通过注解的方式来将Bean加入到指定的Profile中，也可以通过XML的形式
4. `Environment`主要决定哪个Profile要被激活，在没有激活的Profile时要使用哪个作为默认的Profile

###### 注解方式（@Profile）

> **1、简单使用**

```java
@Component
@Profile("prd")
public class DmzService {
	public DmzService() {
		System.out.println("DmzService in prd");
	}
}

@Component
@Profile("dev")
public class IndexService {
	public IndexService(){
		System.out.println("IndexService in dev");
	}
}

public static void main(String[] args) {
  AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
  ac.register(ProfileConfig.class);
  
  //ac.getEnvironment().setActiveProfiles("prd");
  //ac.refresh();  	   // 输出 DmzService in prd
  
  ac.getEnvironment().setActiveProfiles("dev");
  ac.refresh(); 		   // 输出 IndexService in dev
}
```

在上面的例子中，我们给两个组件（`DmzService`，`IndexService`）配置了不同的profile，可以看到当我们利用Environment激活不同的Profile时，可以分别只创建不同的两个类。

在实际生产环境中，我们往往会将`"prd"`，`"dev"`这种代表环境的标签放到系统环境变量中，这样依赖于不同系统的同一环境变量，我们就可以将应用程序运行在不同的profile下。

> **2、结合逻辑运算符使用**

有时间我们某一组件可能同时要运行在多个profile下，这个时候逻辑运算符就派上用场了，我们可以通过如下的三种运行符，对profile进行逻辑运算

- `!`: 非，只有这个profile不被激活才能生效
- `&`: 两个profile同时激活才能生效
- `|`: 只要其中一个profile激活就能生效

比如在上面的例子中，我们可以新增两个类，如下：

```java
@Component
@Profile("dev & qa")
public class LuBanService {
	public LuBanService(){
		System.out.println("LuBanService in dev & qa");
	}
}

@Component("!prd")
public class ProfileService {
	public ProfileService(){
		System.out.println("ProfileService in !prd");
	}
}

public static void main(String[] args) {
AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
ac.register(ProfileConfig.class);
//		ac.getEnvironment().setActiveProfiles("prd");
//		ac.refresh();  // 输出 DmzService in prd
//		ac.getEnvironment().setActiveProfiles("dev");
//		ac.refresh(); // 输出 IndexService in dev
ac.getEnvironment().setActiveProfiles("dev","qa");
ac.refresh();// 输出IndexService in dev
//LuBanService in dev & qa
//ProfileService in !prd
}
```

为了编码的语义，有时候我们会将不同的profile封装成不同的注解，如下：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Profile("production")
public @interface Production {
}
```

有时候可能我们要将多个Bean同时置于某个profile下，这个时候每个Bean添加一个`@Profile`注解显得过去麻烦，这个时候如果我们是采用`@Bean`方式申明的Bean，可以直接在配置类上添加`@Profile`注解，如下（这里我直接取官网中的例子了，就不做验证了）：

```java
@Configuration
public class AppConfig {

    @Bean("dataSource")
    @Profile("development") 
    public DataSource standaloneDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:com/bank/config/sql/schema.sql")
            .addScript("classpath:com/bank/config/sql/test-data.sql")
            .build();
    }

    @Bean("dataSource")
    @Profile("production") 
    public DataSource jndiDataSource() throws Exception {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
    }
}
```

> 3、注意一种特殊的场景

如果我们对使用了@Bean注解的方式进行了重载，那么要求所有重载的方法都在同一个@Profile下，否则@Profile的语义会被覆盖。什么意思呢？大家看下面这个Demo

```java
public class A {
	public A() {
		System.out.println("independent A");
	}
	public A(B b) {
		System.out.println("dependent A with B");
	}
}

public class B {

}

@Configuration
public class ProfileConfig {
	@Bean
	@Profile("dev")
	public A a() {
		return new A();
	}

	@Bean
	@Profile("prd")
	public A a(B b) {
		return new A(b);
	}

	@Bean
	@Profile("prd | dev")
	public B b() {
		return new B();
	}
}

public static void main(String[] args) {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
    ac.register(ProfileConfig.class);
    ac.getEnvironment().setActiveProfiles("dev");
    ac.refresh(); // 输出：dependent A with B
}
```

我们明明激活的是`dev`这个profile，为什么创建的用的带参的构造函数呢？这是因为Spring在创建Bean时，方法的优先级高于Profile，前提是方法的参数在Spring容器内（在上面的例子中，如果我们将B的profile限定为`dev`，那么创建的A就会是通过空参构造创建的）。这里暂且不多说，大家知道有这种场景存在即可。在后面分析源码时我们会介绍，这里涉及到Spring对创建Bean的方法的推断（既包括构造函数也包括`factroyMethod`）。

###### XML方式

```xml
<!--在beans标签中指定profile属性即可-->
<beans profile="development"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xsi:schemaLocation="...">

    <jdbc:embedded-database id="dataSource">
        <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
        <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
    </jdbc:embedded-database>
</beans>
```

XML方式不多赘述，大家有需要可以自行研究

##### 4、PropertySource

通过我们的`Environment`对象，除了能操作profile对象之外，通过之前的继承结构我们知道，他还能进行一些关于属性的操作。而这些操作是建立在Spring本身对运行环境中的一些属性文件的抽象而来的。抽象而成的结果就是`PropertySource`。

###### 接口定义

```java
public abstract class PropertySource<T> {
    protected final  String name;//属性源名称
    protected final T source;//属性源（比如来自Map，那就是一个Map对象）
    public String getName();  //获取属性源的名字  
    public T getSource();        //获取属性源  
    public boolean containsProperty(String name);  //是否包含某个属性  
    public abstract Object getProperty(String name);   //得到属性名对应的属性值   
    // 返回一个ComparisonPropertySource对象
    public static PropertySource<?> named(String name) {
		return new ComparisonPropertySource(name);
	}
} 
```

除了上面定义的这些方法外，`PropertySource`中还定义了几个静态内部类，我们在下面的`UML`类图分析时进行介绍

###### UML类图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200316004002432.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

从上图中可以看到，基于`PropertySource`子类主要可以分为两类，一类是`StubPropertySource`，另一类是`EnumerablePropertySource`。而`StubPropertySource`这一类都是申明于`PropertySource`中的静态内部类。这两个类主要是为了完成一些特殊的功能而设计的。

1. `StubPropertySource`：这个类主要起到类似一个占位符的作用，例如，一个基于`ServletContext`的`PropertySource`必须等待，直到`ServletContext`对象对这个`PropertySource`所在的上下文可用。在这种情况下，需要用到`StubPropertySource`来预设这个`PropertySource`的位置跟顺序，之后在上下文刷新时期，再用一个`ServletContextPropertySourc`来进行替换
2. `ComparisonPropertySource`：这个类设计的目的就是为了进行比较，除了`hashCode()`，`equals()`，`toString()`方法能被调用外，其余方法被调用时均会抛出异常

而`PropertySource`的另外一些子类，都是继承自`EnumerablePropertySource`的，我们先来看`EnumerablePropertySource`这个类对其父类`PropertySource`进行了哪些补充，其定义如下：

```java
public abstract class EnumerablePropertySource<T> extends PropertySource<T> {

	public EnumerablePropertySource(String name, T source) {
		super(name, source);
	}

	protected EnumerablePropertySource(String name) {
		super(name);
	}
	
    // 复写了这个方法
	@Override
	public boolean containsProperty(String name) {
		return ObjectUtils.containsElement(getPropertyNames(), name);
	}
	
    // 新增了这个方法
	public abstract String[] getPropertyNames();

}
```

这个类跟我们`PropertySource`的区别在于：

1. 复写了`containsProperty`这个方法
2. 新增了一个`getPropertyNames`方法

并且我们可以看到，再`containsProperty`这个方法中调用了`getPropertyNames`，这么做的理由是什么呢？为什么它不直接使用父类的`containsProperty`方法而要自己复写一个呢？我们对比下父类的实现：

```java
public boolean containsProperty(String name) {
    return (getProperty(name) != null);
}
```

结合这个类上的一段`javadoc`，如下：

> A {@link PropertySource} implementation capable of interrogating its
> underlying source object to enumerate all possible property name/value
> pairs. Exposes the {@link #getPropertyNames()} method to allow callers
> to introspect available properties without having to access the underlying
> source object. This also facilitates a more efficient implementation of
> {@link #containsProperty(String)}, in that it can call {@link #getPropertyNames()}
> and iterate through the returned array rather than attempting a call to
> {@link #getProperty(String)} which may be more expensive. Implementations may
> consider caching the result of {@link #getPropertyNames()} to fully exploit this
> performance opportunity.

Spring设计这个类的主要目的是为了，让调用者可以不访问其中的`Source`对象但是能判断这个`PropertySource`中是否包含了指定的key，所以它多提供了一个`getPropertyNames`，同时这段`javadoc`还指出，子类的实现应该考虑去缓存`getPropertyNames`这个方法的返回值去尽可能的压榨性能。

接下来，我们分别看一看它的各个实现类

- `MapPropertySource`

`MapPropertySource`的source来自于一个Map，这个类结构很简单，这里不说。
用法如下：

```java
public static void main(String[] args) {
    Map<String,Object> map=new HashMap<>();
    map.put("name","wang");
    map.put("age",23);
    MapPropertySource source_1=new MapPropertySource("person",map);
    System.out.println(source_1.getProperty("name"));//wang
    System.out.println(source_1.getProperty("age"));//23
    System.out.println(source_1.getName());//person
    System.out.println(source_1.containsProperty("class"));//false
}
```

- `ResourcePropertySource`

source是一个Properties对象，继承自`MapPropertySource`。与`MapPropertySource`用法相同

- `ServletConfigPropertySource`

source为`ServletConfig`对象

- `ServletContextPropertySource`

source为`ServletContext`对象

- `SystemEnvironmentPropertySource`

继承自`MapPropertySource`，它的source也是一个map，但来源于系统环境。

- `CompositePropertySource`

内部可以保存多个`PropertySource`

```java
private final Set<PropertySource<?>> propertySources = new LinkedHashSet<PropertySource<?>>();
```

取值时依次遍历这些`PropertySource`

###### PropertySources

我们在阅读`PropertySource`源码上，会发现在其上有一段这样的javaDoc解释，其中提到了

> {@code PropertySource} objects are not typically used in isolation, but rather through a {@link PropertySources} object, which aggregates property sources and in conjunction with a {@link PropertyResolver} implementation that can perform precedence-based searches across the set of {@code PropertySources}. 

也就是说，`PropertySource`通常都不会单独的使用，而是通过`PropertySources`对象。

- 接口定义

```java
public interface PropertySources extends Iterable<PropertySource<?>> {

	default Stream<PropertySource<?>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
    
	boolean contains(String name);

	@Nullable
	PropertySource<?> get(String name);

}
```

这个接口由于继承了`Iterable`接口，所以它的子类也具备了迭代能力。

- 唯一子类

```java
public class MutablePropertySources implements PropertySources {
private final List<PropertySource<?>> propertySourceList = new CopyOnWriteArrayList<>();
......
}
```

这个类最大的特点就是，持有了一个保存`PropertySource`的`CopyOnWriteArrayList`集合。并且它其余提供的方法，都是在往集合中增删`PropertySource`。

##### 5、PropertyResolver

在之前的[Environment的接口定义](https://daimingzhi.blog.csdn.net/article/details/104890350#jiekou)中我们知道，`Environment`接口继承了`PropertyResolver`接口，接下来我们再来关注下这个接口的定义

###### 接口定义

```java
public interface PropertyResolver {
	// 当前的环境中是否包含这个属性
	boolean containsProperty(String key);
	
    //获取属性值 如果找不到返回null   
	@Nullable
	String getProperty(String key);
	
    // 获取属性值，如果找不到返回默认值        
	String getProperty(String key, String defaultValue);
	
    // 获取指定类型的属性值，找不到返回null  
	@Nullable
	<T> T getProperty(String key, Class<T> targetType);
	
    // 获取指定类型的属性值，找不到返回默认值  
	<T> T getProperty(String key, Class<T> targetType, T defaultValue);
	
    // 获取属性值，找不到抛出异常IllegalStateException  
	String getRequiredProperty(String key) throws IllegalStateException;
	
    // 获取指定类型的属性值，找不到抛出异常IllegalStateException         
	<T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;
	
    // 替换文本中的占位符（${key}）到属性值，找不到不解析  
	String resolvePlaceholders(String text);
    
    // 替换文本中的占位符（${key}）到属性值，找不到抛出异常IllegalArgumentException 
	String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
```

###### UML类图

它的实现类主要有两种：

1. 各种Resolver：主要是`PropertySourcesPropertyResolver`
2. 各种`Environment`

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200316004031763.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

- `PropertySourcesPropertyResolver`使用示例

```java
MutablePropertySources sources = new MutablePropertySources();
sources.addLast(new MapPropertySource("map", new HashMap<String, Object>() {
    {
        put("name", "wang");
        put("age", 12);
    }
}));//向MutablePropertySources添加一个MapPropertySource

PropertyResolver resolver = new PropertySourcesPropertyResolver(sources);
System.out.println(resolver.containsProperty("name"));//输出 true
System.out.println(resolver.getProperty("age"));//输出 12
System.out.println(resolver.resolvePlaceholders("My name is ${name} .I am ${age}."));
```

- 关于`Environment`实现主要分为两种

1. `StandardEnvironment`,标准环境，普通Java应用时使用，会自动注册`System.getProperties()`和 `System.getenv()`到环境
2. `StandardServletEnvironment`：标准`Servlet`环境，其继承了`StandardEnvironment`，Web应用时使用，除了`StandardEnvironment`外，会自动注册`ServletConfig（DispatcherServlet）`、`ServletContext`及有选择性的注册`JNDI`实例到环境

------

### 总结

 在这篇文章中，我们学习了`ApplicationContext`的部分知识，首先我们知道`ApplicationContext`继承了[5类接口](https://daimingzhi.blog.csdn.net/article/details/104890350#extends)，正由于继承了这五类接口，所以它具有了以下这些功能：

- `MessageSource`，主要用于国际化
- `ApplicationEventPublisher`，提供了事件发布功能
- `EnvironmentCapable`，可以获取容器当前运行的环境
- `ResourceLoader`，主要用于加载资源文件
- `BeanFactory`，负责配置、创建、管理Bean，`IOC`功能的实现主要就依赖于该接口子类实现。

 在上文，我们分析学习了国际化，以及Spring中环境的抽象（`Environment`）。对于国际化而言，首先我们要知道国际化到底是什么？简而言之，国际化就是**为每种语言提供一套相应的资源文件，并以规范化命名的方式保存在特定的目录中，由系统自动根据客户端语言选择适合的资源文件**。其次，我们也一起了解了java中的国际化，最后学习了Spring对java国际化的一些封装，也就是`MessageSource`接口

 对于[Spring中环境的抽象（`Environment`）](https://daimingzhi.blog.csdn.net/article/details/104890350#env)这块内容比较多，主要要知道`Environment`完成了两个功能

- 为程序运行提供不同的剖面，即`Profile`
- 操作程序运行中的属性资源

整个`Environment`体系可以用下图表示

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020031600404125.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

对上图的解释：

1. `Environment`可以激活不同的Profile而为程序选择不同的剖面，一个Profile其实就是一组Spring中的Bean
2. `Environment`继承了`PropertyResolver`，从而可以操作程序运行时中的属性资源。而`PropertyResolver`的实现依赖于`PropertySource`，同时`PropertySource`一般不会独立使用，而是被封装进一个`PropertySources`对象中。

## Spring官网阅读（十二）ApplicationContext详解（中）



> 在上篇文章中我们已经对ApplicationContext的一部分内容做了介绍，ApplicationContext主要具有以下几个核心功能：
>
> 1. 国际化
> 2. 借助Environment接口，完成了对Spring运行环境的抽象，可以返回环境中的属性，并能出现出现的占位符
> 3. 借助于Resource系列接口，完成对底层资源的访问及加载
> 4. 继承了ApplicationEventPublisher接口，能够进行事件发布监听
> 5. 负责创建、配置及管理Bean
>
> 在上篇文章我们已经分析学习了1，2两点，这篇文章我们继续之前的学习

### 1、Spring的资源（Resource）

首先需要说明的是，Spring并没有让ApplicationContext直接继承Resource接口，就像ApplicationContext接口也没有直接继承Environment接口一样。这应该也不难理解，采用这种组合的方式会让我们的类更加的轻量，也起到了解耦的作用。ApplicationContext跟Resource相关的接口的继承关系如下

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200317234931371.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

不管是ResourceLoader还是ResourcePatternResolver其实都是为了获取Resource对象，不过ResourcePatternResolver在ResourceLoader的基础上扩展了一个获取多个Resource的方法，我们在后文会介绍。

#### 接口简介

Resouce接口继承了 InputStreamSource.

```java
public interface InputStreamSource {
    // 每次调用都将返回一个当前资源对应的java.io. InputStream字节流
    InputStream getInputStream() throws IOException;
}
1234
public interface Resource extends InputStreamSource {  

	// 用于判断对应的资源是否真的存在
	boolean exists();

	// 用于判断对应资源的内容是否可读。需要注意的是当其结果为true的时候，其内容未必真的可读，但如果返回false，则其内容必定不可读
	default boolean isReadable() {
		return exists();
	}

	// 用于判断当前资源是否代表一个已打开的输入流，如果结果为true，则表示当前资源的输入流不可多次读取，而且在读取以后需要对它进行关闭，以防止内存泄露。该方法主要针对于实现类InputStreamResource，实现类中只有它的返回结果为true，其他都为false。
	default boolean isOpen() {
		return false;
	}
	
    // 当前资源是否是一个文件
	default boolean isFile() {
		return false;
	}

	//当前资源对应的URL。如果当前资源不能解析为一个URL则会抛出异常
	URL getURL() throws IOException;

	//当前资源对应的URI。如果当前资源不能解析为一个URI则会抛出异常
	URI getURI() throws IOException;

	// 返回当前资源对应的File。如果当前资源不能以绝对路径解析为一个File则会抛出异常。
	File getFile() throws IOException;

	// 返回一个ReadableByteChannel
	default ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}

	//  返回资源的长度
	long contentLength() throws IOException;

	// 最后修改时间
	long lastModified() throws IOException;

	// 根据当前资源以及相对当前资源的路径创建一个新的资源，比如当前Resource代表文件资源“d:/abc/a.java”,则createRelative（“xyz.txt”）将返回表文件资源“d:/abc/xyz.txt”
	Resource createRelative(String relativePath) throws IOException;

	// 返回文件一个文件名称，通常来说会返回该资源路径的最后一段
	@Nullable
	String getFilename();

	// 返回描述信息
	String getDescription();
}
```

#### UML类图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200317234805560.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

因为实现了Resource接口的类很多，并且一些类我们也不常用到或者很简单，所以上图中省略了一些不重要的分支，接下来我们就一个个分析。

##### 抽象基类AbstractResource

实现了Resource接口，是大多数Resource的实现类的基类，提供了很多通用的方法。
比如exists方法会检查是否一个文件或者输入流能够被打开。isOpen永远返回false。”getURL()” 和”getFile()”方法会抛出异常。toString将会返回描述信息。

##### FileSystemResource

基于java的文件系统封装而成的一个资源对象。

##### AbstractFileResolvingResource

将URL解析成文件引用，既会处理协议为：“file“的URL，也会处理JBoss的”vfs“协议。然后相应的解析成对应的文件系统引用。

##### ByteArrayResource

根据一个给定的字节数组构建的一个资源。同时给出一个对应的输入流

##### BeanDefinitionResource

只是对BeanDefinition进行的一次描述性的封装

##### InputStreamResource

是针对于输入流封装的资源，它的构建需要一个输入流。 对于“getInputStream ”操作将直接返回该字节流，因此只能读取一次该字节流，即“isOpen”永远返回true。

##### UrlResource

UrlResource代表URL资源，用于简化URL资源访问。
UrlResource一般支持如下资源访问：
-http：通过标准的http协议访问web资源，如new UrlResource(“http://地址”)；
-ftp：通过ftp协议访问资源，如new UrlResource(“ftp://地址”)；
-file：通过file协议访问本地文件系统资源，如new UrlResource(“file:d:/test.txt”)；

##### ClassPathResource

JDK获取资源有两种方式

1. 使用Class对象的getResource(String path)获取资源URL，getResourceAsStream(String path)获取资源流。 参数既可以是当前class文件相对路径（以文件夹或文件开头），也可以是当前class文件的绝对路径（以“/”开头,相对于当前classpath根目录）
2. 使用ClassLoader对象的getResource(String path)获取资源URL，getResourceAsStream(String path)获取资源流。参数只能是绝对路径，但不以“/”开头

ClassPathResource代表classpath路径的资源，将使用给定的Class或ClassLoader进行加载classpath资源。 “isOpen”永远返回false，表示可多次读取资源。

##### ServletContextResource

是针对于ServletContext封装的资源，用于访问ServletContext环境下的资源。ServletContextResource持有一个ServletContext的引用，其底层是通过ServletContext的getResource()方法和getResourceAsStream()方法来获取资源的。

#### ResourceLoader

##### 接口简介

ResourceLoader接口被设计用来从指定的位置加载一个Resource,其接口定义如下

```java
public interface ResourceLoader {
	
    // classpath:
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;
	
    // 核心方法，从指定位置加载一个Resource
    // 1.支持权限的的URL格式，如：file:C:/test.dat
    // 2.支持classpath的格式,如：classpath:test.dat
    // 3.支持文件相对路径，如：WEB-INF/test.dat
	Resource getResource(String location);
	
    // 返回用于加载该资源的ClassLoader
	@Nullable
	ClassLoader getClassLoader();

}

1234567891011121314151617
```

##### UML类图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200317234817867.png)

对于一些不是很必要的类我都省略了，其实核心的类我们只需要关注`DefaultResourceLoader`就可以了，因为其余子类（除了`GenericApplicationContext`）都是直接继承了`DefaultResourceLoader`的`getResource`方法。代码如下：

```java
	@Override
	public Resource getResource(String location) {
		Assert.notNull(location, "Location must not be null");
		
        // 正常来说protocolResolvers集合是空的，除非我们调用了它的addProtocolResolver方法添加了自定义协议处理器，调用addProtocolResolver方法所添加的协议处理器会覆盖原有的处理逻辑
		for (ProtocolResolver protocolResolver : this.protocolResolvers) {
			Resource resource = protocolResolver.resolve(location, this);
			if (resource != null) {
				return resource;
			}
		}
		
        // 如果是以“/”开头，直接返回一个classpathResource
		if (location.startsWith("/")) {
			return getResourceByPath(location);
		}
        // 如果是形如：classpath:test.dat也直接返回一个ClassPathResource
		else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
			return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
		}
		else {
			try {
				// 否则将其解析为一个URL
				URL url = new URL(location);
                // 如果是一个文件，直接返回一个FileUrlResource，否则返回一个普通的UrlResource
				return (ResourceUtils.isFileURL(url) ? new FileUrlResource(url) : new UrlResource(url));
			}
			catch (MalformedURLException ex) {
				// 如果URL转换失败，还是作为一个普通的ClassPathResource
				return getResourceByPath(location);
			}
		}
	}
123456789101112131415161718192021222324252627282930313233
```

##### 资源路径

###### ant-style

类似下面这种含有通配符的路径

```xml
/WEB-INF/*-context.xml
com/mycompany/**/applicationContext.xml
file:C:/some/path/*-context.xml
classpath:com/mycompany/**/applicationContext.xml
1234
```

###### classpath跟classpath*

classpath:用于加载类路径（包括jar包）中的一个且仅一个资源；

classpath*:用于加载类路径（包括jar包）中的所有匹配的资源，可使用Ant路径模式。

### 2、Spring中的事件监听机制（publish-event）

我们知道，ApplicationContext接口继承了ApplicationEventPublisher接口，能够进行事件发布监听，那么什么是事件的发布跟监听呢？我们从监听者模式说起

##### 监听者模式

###### 概念

事件源经过事件的封装传给监听器，当事件源触发事件后，监听器接收到事件对象可以回调事件的方法

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200317234830949.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

###### Spring对监听者模式的实践

我们直接通过一个例子来体会下

```java
public class Main {
	public static void main(String[] args) {
		// 创建一个事件发布器（事件源），为了方便，我这里直接通过传入EventListener.class来将监听器注册到容器中
		ApplicationEventPublisher ac = new AnnotationConfigApplicationContext(EventListener.class);
		// 发布一个事件
		ac.publishEvent(new MyEvent("hello event"));
		// 程序会打印如下：
        // 接收到事件：hello event
		// 处理事件....
	}

	static class MyEvent extends ApplicationEvent {
		public MyEvent(Object source) {
			super(source);
		}
	}

	@Component
	static class EventListener implements ApplicationListener<MyEvent> {
		@Override
		public void onApplicationEvent(MyEvent event) {
			System.out.println("接收到事件：" + event.getSource());
			System.out.println("处理事件....");
		}
	}
}
1234567891011121314151617181920212223242526
```

在上面的例子中，主要涉及到了三个角色，也就是我们之前提到的

1. 事件源：ApplicationEventPublisher
2. 事件：MyEvent，继承了ApplicationEvent
3. 事件监听器：EventListener，实现了ApplicationListener

我们通过ApplicationEventPublisher发布了一个事件（MyEvent），然后事件监听器监听到了事件，并进行了对应的处理。

###### 接口简介

- ##### ApplicationEventPublisher

```java
public interface ApplicationEventPublisher {
	
	default void publishEvent(ApplicationEvent event) {
		publishEvent((Object) event);
	}
	
    // 从版本4.2后新增的方法
    // 调用这个方法发布的事件不需要实现ApplicationEvent接口，会被封装成一个PayloadApplicationEvent
    // 如果event实现了ApplicationEvent接口，则会正常发布
	void publishEvent(Object event);

}
123456789101112
```

对于这个接口，我只需要关注有哪些子类是实现了`publishEvent(Object event)`这个方法即可。搜索发现，我们只需要关注`org.springframework.context.support.AbstractApplicationContext#publishEvent(java.lang.Object)`这个方法即可，关于这个方法在后文的源码分析中我们再详细介绍。

- ##### ApplicationEvent

继承关系如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200317234850107.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

我们主要关注上面4个类（*PayloadApplicationEvent*在后文源码分析时再介绍），下面几个都是Spring直接在内部使用到了的事件，比如ContextClosedEvent，在容器关闭时会被创建然后发布。

```java
// 这个类在设计时是作为整个应用内所有事件的基类，之所以被设计成抽象类，是因为直接发布这个对象没有任何意义
public abstract class ApplicationEvent extends EventObject {
	
    // 事件创建的事件
	private final long timestamp;

	public ApplicationEvent(Object source) {
		super(source);
		this.timestamp = System.currentTimeMillis();
	}

	public final long getTimestamp() {
		return this.timestamp;
	}

}

// 这个类是java的util包下的一个类，java本身也具有一套事件机制
public class EventObject implements java.io.Serializable {
	
    // 事件所发生的那个源，比如在java中，我们发起了一个鼠标点击事件，那么这个source就是鼠标
    protected transient Object  source;

    public EventObject(Object source) {
        if (source == null)
            throw new IllegalArgumentException("null source");
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

    public String toString() {
        return getClass().getName() + "[source=" + source + "]";
    }
}

// 这个类是2.5版本时增加的一个类，相对于它直接的父类ApplicationEvent而言，最大的区别就是
// 将source规定为了当前的容器。就目前而言的话这个类作用不大了，一般情况下我们定义事件也不一定需要继承这个ApplicationContextEvent
// 后面我会介绍注解的方式进行事件的发布监听
public abstract class ApplicationContextEvent extends ApplicationEvent {

	public ApplicationContextEvent(ApplicationContext source) {
		super(source);
	}

	public final ApplicationContext getApplicationContext() {
		return (ApplicationContext) getSource();
	}

}

1234567891011121314151617181920212223242526272829303132333435363738394041424344454647484950515253
```

- ##### ApplicationListener

```java
// 事件监听器，实现了java.util包下的EventListener接口
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

	// 根据接口申明的泛型类型处理对应的事件
    // 比如在我们之前的例子中，通过《EventListener implements ApplicationListener<MyEvent>》
    // 在接口中申明了泛型类型为MyEvent，所以能监听到MyEvent这一类事件
	void onApplicationEvent(E event);

}
123456789
```

###### 注解方式实现事件发布机制

在上面的例子中，我们通过传统的方式实现了事件的发布监听，但是上面的过程实在是有点繁琐，我们发布的事件需要实现指定的接口，在进行监听时又需要实现指定的接口。每增加一个发布的事件，代表我们需要多两个类。这样在项目的迭代过程中，会导致我们关于事件的类越来越多。所以，在Spring4.2版本后，新增一个注解，让我们可以快速的实现对发布的事件的监听。示例代码如下：

```java
@ComponentScan("com.dmz.official.event")
public class Main02 {
	public static void main(String[] args) {
		ApplicationEventPublisher publisher = new AnnotationConfigApplicationContext(Main02.class);
		publisher.publishEvent(new Event("注解事件"));
        // 程序打印：
        // 接收到事件:注解事件
        // 处理事件
	}

	static class Event {
		String name;

		Event(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@Component
	static class Listener {
		@EventListener
		public void listen(Event event) {
			System.out.println("接收到事件:" + event);
			System.out.println("处理事件");
		}
	}
}
1234567891011121314151617181920212223242526272829303132
```

可以看到在上面的例子中，我们使用一个`@EventListener`注解，直接标注了Listener类中的一个方法是一个事件监听器，并且通过方法的参数类型Event指定了这个监听器监听的事件类型为Event类型。在这个例子中，第一，我们事件不需要去继承特定的类，第二，我们的监听器也不需要去实现特定的接口，极大的方便了我们的开发。

###### 异步的方式实现事件监听

对于上面的例子，我们只需要按下面这种方式添加两个注解即可实现异步：

```java
@ComponentScan("com.dmz.official.event")
@Configuration
@EnableAsync  // 1.开启异步支持
public class Main02 {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext publisher = new AnnotationConfigApplicationContext(Main02.class);
		publisher.publishEvent(new Event("注解事件"));
	}

	static class Event {
		String name;

		Event(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@Component
	static class Listener {
		@EventListener
		@Async
        // 2.标志这个方法需要异步执行
		public void listen(Event event) {
			System.out.println("接收到事件:" + event);
			System.out.println("处理事件");
		}
	}
}
123456789101112131415161718192021222324252627282930313233
```

对于上面的两个注解`@EnableAsync`以及`@Async`，我会在AOP系列的文章中再做介绍，目前而言，大家知道能通过这种方式开启异步支持即可。

###### 对监听器进行排序

当我们发布一个事件时，可能会同时被两个监听器监听到，比如在我们上面的例子中如果同时存在两个监听器，如下：

```java
@Component
static class Listener {
    @EventListener
    public void listen1(Event event) {
        System.out.println("接收到事件1:" + event);
        System.out.println("处理事件");
    }

    @EventListener
    public void listen2(Event event) {
        System.out.println("接收到事件2:" + event);
        System.out.println("处理事件");
    }
}
1234567891011121314
```

在这种情况下，我们可能希望两个监听器可以按顺序执行，这个时候需要用到另外一个注解了:`@Order`

还是上面的代码，我们添加注解如下：

```java
@Component
static class Listener {
    @EventListener
    @Order(2)
    public void listen1(Event event) {
        System.out.println("接收到事件1:" + event);
        System.out.println("处理事件");
    }

    @EventListener
    @Order(1)
    public void listen2(Event event) {
        System.out.println("接收到事件2:" + event);
        System.out.println("处理事件");
    }
}
12345678910111213141516
```

注解中的参数越小，代表优先级越高，在上面的例子中，会执行listen2方法再执行listen1方法

------

那么Spring到底是如何实现的这一套事件发布机制呢？接下来我们进行源码分析

###### 源码分析（publishEvent方法）

我们需要分析的代码主要是`org.springframework.context.support.AbstractApplicationContext#publishEvent(java.lang.Object, org.springframework.core.ResolvableType)方法`，源码如下：

```java
protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
    Assert.notNull(event, "Event must not be null");

    // 如果发布的事件是一个ApplicationEvent，直接发布
    ApplicationEvent applicationEvent;
    if (event instanceof ApplicationEvent) {
        applicationEvent = (ApplicationEvent) event;
    }
    else {
        // 如果发布的事件不是一个ApplicationEvent，包装成一个PayloadApplicationEvent
        applicationEvent = new PayloadApplicationEvent<>(this, event);
        // 我们在应用程序中发布事件时，这个eventType必定为null
        if (eventType == null) {
            // 获取对应的事件类型
            eventType = ((PayloadApplicationEvent) applicationEvent).getResolvableType();
        }
    }
    // 我们在自己的项目中调用时，这个earlyApplicationEvents必定为null
    if (this.earlyApplicationEvents != null) {
        this.earlyApplicationEvents.add(applicationEvent);
    }
    else {
        // 获取事件发布器，发布对应的事件
        getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
    }

    // 父容器中也需要发布事件
    if (this.parent != null) {
        if (this.parent instanceof AbstractApplicationContext) {
            ((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
        }
        else {
            this.parent.publishEvent(event);
        }
    }
}
123456789101112131415161718192021222324252627282930313233343536
```

上面这段代码核心部分就是`getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);`，我们分成以下几个部分分析

- getApplicationEventMulticaster()方法
- multicastEvent()方法

getApplicationEventMulticaster()方法

代码如下：

```java
ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
    if (this.applicationEventMulticaster == null) {
        throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
                                        "call 'refresh' before multicasting events via the context: " + this);
    }
    return this.applicationEventMulticaster;
}
1234567
```

可以看到，只是简单的获取容器中已经初始化好的一个`ApplicationEventMulticaster`，那么现在有以下几问题。

1、ApplicationEventMulticaster是什么？

- 接口定义

```java
public interface ApplicationEventMulticaster {
	// 添加事件监听器
	void addApplicationListener(ApplicationListener<?> listener);

	// 通过名称添加事件监听器
	void addApplicationListenerBean(String listenerBeanName);

	// 移除事件监听器
	void removeApplicationListener(ApplicationListener<?> listener);

	// 根据名称移除事件监听器
	void removeApplicationListenerBean(String listenerBeanName);

	// 移除注册在这个事件分发器上的所有监听器
	void removeAllListeners();

	// 分发事件
	void multicastEvent(ApplicationEvent event);

	// 分发事件，eventType代表事件类型，如果eventType为空，会从事件对象中推断出事件类型
	void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType);

}
1234567891011121314151617181920212223
```

- UML类图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200317234903765.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

主要涉及到两个类：

1. `AbstractApplicationEventMulticaster`,这个类对`ApplicationEventMulticaster`这个接口基础方法做了实现，除了其核心方法`multicastEvent`。这个类最大的作用是获取监听器，稍后我们会介绍。
2. `SimpleApplicationEventMulticaster`,这是Spring默认提供的一个事件分发器，如果我们没有进行特别的配置的话，就会采用这个类生成的对象作为容器的事件分发器。

2、容器在什么时候对其进行的初始化

回到我们之前的一张图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200317234913778.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

可以看到，在第`3-8`步调用了一个`initApplicationEventMulticaster`方法，从名字上我们就知道，这是对`ApplicationEventMulticaster`进行初始化的，我们看看这个方法做了什么。

- initApplicationEventMulticaster方法

```java
protected void initApplicationEventMulticaster() {
    ConfigurableListableBeanFactory beanFactory = getBeanFactory();
    // 判断容器中是否包含了一个名为applicationEventMulticaster的ApplicationEventMulticaster类的对象，如果包含，直接获取即可。
    if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
        this.applicationEventMulticaster =
            beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
		// 删除不必要的日志
    }
    // 如果没有包含，new一个SimpleApplicationEventMulticaster并将其注册到容器中
    else {
        this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
        beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
       // 删除不必要的日志
        }
    }
}
12345678910111213141516
```

这段代码的含义就是告诉我们，可以自己配置一个applicationEventMulticaster，如果没有进行配置，那么将默认使用一个SimpleApplicationEventMulticaster。

接下来，我们尝试自己配置一个简单的applicationEventMulticaster，示例代码如下：

```java
@Component("applicationEventMulticaster")
static class MyEventMulticaster extends AbstractApplicationEventMulticaster {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void multicastEvent(@NonNull ApplicationEvent event) {
        ResolvableType resolvableType = ResolvableType.forInstance(event);
        Collection<ApplicationListener<?>> applicationListeners = getApplicationListeners(event, resolvableType);
        for (ApplicationListener applicationListener : applicationListeners) {
            applicationListener.onApplicationEvent(event);
        }
    }

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        System.out.println("进入MyEventMulticaster");
    }
}
123456789101112131415161718
```

运行程序后会发现“进入MyEventMulticaster”这句话打印了两次，这是一次是容器启动时会发布一个ContextStartedEvent事件，也会调用我们配置的事件分发器进行事件发布。

multicastEvent方法

在Spring容器中，只内置了一个这个方法的实现类，就是SimpleApplicationEventMulticaster。实现的逻辑如下：

```java
public void multicastEvent(ApplicationEvent event) {
    multicastEvent(event, resolveDefaultEventType(event));
}

@Override
public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
    ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
    for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
        Executor executor = getTaskExecutor();
        if (executor != null) {
            executor.execute(() -> invokeListener(listener, event));
        }
        else {
            invokeListener(listener, event);
        }
    }
}
1234567891011121314151617
```

上面的代码主要的实现逻辑可以分为这么几步：

1. 推断事件类型
2. 根据事件类型获取对应的监听器
3. 执行监听逻辑

我们一步步分析

- resolveDefaultEventType(event)，推断事件类型

```java
private ResolvableType resolveDefaultEventType(ApplicationEvent event) {
    return ResolvableType.forInstance(event);
}

public static ResolvableType forInstance(Object instance) {
    Assert.notNull(instance, "Instance must not be null");
    if (instance instanceof ResolvableTypeProvider) {
        ResolvableType type = ((ResolvableTypeProvider) instance).getResolvableType();
        if (type != null) {
            return type;
        }
    }
    // 返回通过事件的class类型封装的一个ResolvableType
    return ResolvableType.forClass(instance.getClass());
}
123456789101112131415
```

上面的代码涉及到一个概念就是ResolvableType，对于ResolvableType我们需要了解的是，ResolvableType为所有的java类型提供了统一的数据结构以及API，换句话说，一个ResolvableType对象就对应着一种java类型。我们可以通过ResolvableType对象获取类型携带的信息（举例如下）：

1. getSuperType()：获取直接父类型
2. getInterfaces()：获取接口类型
3. getGeneric(int…)：获取类型携带的泛型类型
4. resolve()：Type对象到Class对象的转换

另外，ResolvableType的构造方法全部为私有的，我们不能直接new，只能使用其提供的静态方法进行类型获取：

1. forField(Field)：获取指定字段的类型
2. forMethodParameter(Method, int)：获取指定方法的指定形参的类型
3. forMethodReturnType(Method)：获取指定方法的返回值的类型
4. forClass(Class)：直接封装指定的类型
5. ResolvableType.forInstance 获取指定的实例的泛型信息

关于ResolvableType跟java中的类型中的关系请关注我的后续文章，限于篇幅原因在本文就不做过多介绍了。

- getApplicationListeners(event, type)，获取对应的事件监听器

事件监听器主要分为两种，一种是我们通过实现接口直接注册到容器中的Bean，例如下面这种

```java
@Component
static class EventListener implements ApplicationListener<MyEvent> {
    @Override
    public void onApplicationEvent(MyEvent event) {
        System.out.println("接收到事件：" + event.getSource());
        System.out.println("处理事件....");
    }
}
12345678
```

另外一个是通过注解的方式，就是下面这种

```java
@Component
static class Listener {
    @EventListener
    public void listen1(Event event) {
        System.out.println("接收到事件1:" + event);
        System.out.println("处理事件");
    }
}
12345678
```

对于实现接口的方式不用多说，因为实现了这个类本身就会被扫描然后加入到容器中。对于注解这种方式，Spring是通过一个回调方法实现的。大家关注下这个接口`org.springframework.beans.factory.SmartInitializingSingleton`,同时找到其实现类，`org.springframework.context.event.EventListenerMethodProcessor`。在这个类中，会先调用`afterSingletonsInstantiated`方法，然后调用一个`processBean`方法，在这个方法中会遍历所有容器中的所有Bean，然后遍历Bean中的每一个方法判断方法上是否加了一个`@EventListener`注解。如果添加了这个注解，会将这个Method方法包装成一个`ApplicationListenerMethodAdapter`，这个类本身也实现了`ApplicationListener`接口。之后在添加到监听器的集合中。

- invokeListener，执行监听逻辑

本身这个方法没有什么好说的了，就是调用了`ApplicationListener`中的`onApplicationEvent`方法，执行我们的业务逻辑。但是值得注意的是，在调用invokeListener方法前，会先进行一个判断

```java
Executor executor = getTaskExecutor();
if (executor != null) {
    executor.execute(() -> invokeListener(listener, event));
}
else {
    invokeListener(listener, event);
}
1234567
```

会先判断是否能获取到一个Executor，如果能获取到那么会通过这个Executor异步执行监听的逻辑。所以基于这段代码，我们可以不通过@Async注解实现对事件的异步监听，而是复写`SimpleApplicationEventMulticaster`这个类中的方法，如下：

```java
@Component("applicationEventMulticaster")
public class MyEventMulticaster extends SimpleApplicationEventMulticaster {
    @Override
    public Executor getTaskExecutor() {
        // 在这里创建自己的执行器
        return executor();
    }
}

123456789
```

相比于通过`@Async注解实现对事件的异步监听`我更加倾向于这种通过复写方法的方式进行实现，主要原因就是如果通过注解实现，那么所有加了这个注解的方法在异步执行都都是用的同一个线程池，这些加了注解的方法有些可能并不是进行事件监听的，这样显然是不合理的。而后面这种方式，我们可以确保创建的线程池是针对于事件监听的，甚至可以根据不同的事件类型路由到不同的线程池。这样更加合理。

### 3、总结

在这篇文章中，我们完成了对ApplicationContext中以下两点内容的学习

1. 借助于Resource系列接口，完成对底层资源的访问及加载
2. 实现事件的发布

对于整个ApplicationContext体系，目前来说还剩一个很大的功能没有涉及到。因为我们也知道ApplicationContext也继承了一系列的BeanFactory接口。所以它还会负责创建、配置及管理Bean。

BeanFactory本身也有一套自己的体系，在下篇文章中，我们就会学习BeanFactory相关的内容。虽然这一系列文章是以ApplicationContext命名的，但是其中的内容覆盖面很广，这些东西对于我们看懂Spring很重要。

希望大家跟我一起慢慢啃掉Spring，加油！共勉！

## Spring官网阅读（十三）ApplicationContext详解（下）



> 在前面两篇文章中，我们已经对ApplicationContext的大部分内容做了介绍，包括国际化，Spring中的运行环境，Spring中的资源，Spring中的事件监听机制，还剩唯一一个BeanFactory相关的内容没有介绍，这篇文章我们就来介绍BeanFactory，这篇文章结束，关于ApplicationContext相关的内容我们也总算可以告一段落了。本文对应官网中的`1.16`及`1.15`小结

前面我们也提到了ApplicationContext继承了BeanFactory接口，其继承关系如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200330133804279.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

下面我们直接进入BeanFactory相关内容的学习

### BeanFactory

#### 接口定义

```java
public interface BeanFactory {
	
    // FactroyBean的前缀，如果getBean的时候BeanName有这个前缀，会去获取对应的FactroyBean
    // 而不是获取FactroyBean的getObject返回的Bean
	String FACTORY_BEAN_PREFIX = "&";
	
    // 都是用于获取指定的Bean，根据名称获取指定类型获取
	Object getBean(String name) throws BeansException;
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;
	Object getBean(String name, Object... args) throws BeansException;
	<T> T getBean(Class<T> requiredType) throws BeansException;
	<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;
	
    // 获取指定的Bean的ObjectProvider,这个有个问题，ObjectProvider是什么？请参考我《Spring杂谈》相关文章
    <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);
	<T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType);
	
    // 检查容器中是否含有这个名称的Bean
	boolean containsBean(String name);
	
    // 判断指定的Bean是否为单例
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;
	
    // 判断指定的Bean是否为原型
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;
	
    // 判断指定的Bean类型是否匹配，关于ResolvableType我已经专门写文章介绍过了，请参考我《Spring杂谈》相关文章
	boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;
	boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException;
	
    // 返回指定Bean的类型
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;
	
    // 返回指定Bean的别名
	String[] getAliases(String name);

}
12345678910111213141516171819202122232425262728293031323334353637
```

> 可以看到`BeanFactory`接口主要提供了查找Bean，创建Bean（在getBean调用的时候也会去创建Bean）,以及针对容器中的Bean做一些判断的方法（包括是否是原型，是否是单例，容器是否包含这个名称的Bean，是否类型匹配等等）

#### 继承关系

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200330133814364.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

#### 接口功能

作为BeanFactory的直接子接口的有三个，分别是`HierarchicalBeanFactory`,`ListableBeanFactory`,`AutowireCapableBeanFactory`。

##### 1、HierarchicalBeanFactory

```java
public interface HierarchicalBeanFactory extends BeanFactory {
	// 获取父容器
    @Nullable
	BeanFactory getParentBeanFactory();
    // 获取父系容器，只在当前容器中判断是否包含这个名称的Bean
	boolean containsLocalBean(String name);
}
1234567
```

HierarchicalBeanFactory对顶层的BeanFactory做了扩展，让其具有了父子层级关系

##### 2、ListableBeanFactory

```java
public interface ListableBeanFactory extends BeanFactory {
	
    // 1.查找容器中是否包含对应名称的BeanDefinition
    // 2.忽略层级关系，只在当前容器中查找
	boolean containsBeanDefinition(String beanName);

    // 1.查找容器中包含的BeanDefinition的数量
    // 2.忽略层级关系，只在当前容器中查找
	int getBeanDefinitionCount();

    // 1.获取当前容器中所有的BeanDefinition的名称
    // 2.忽略层级关系，只在当前容器中查找
	String[] getBeanDefinitionNames();

	// 根据指定类型获取容器中的对应的Bean的名称，可能会有多个
    // 既会通过BeanDefinition做判断，也会通过FactoryBean的getObjectType方法判断
	String[] getBeanNamesForType(ResolvableType type);
	String[] getBeanNamesForType(@Nullable Class<?> type);
	
    // 根据指定类型获取容器中的对应的Bean的名称，可能会有多个
    // 既会通过BeanDefinition做判断，也会通过FactoryBean的getObjectType方法判断
    // includeNonSingletons：是否能包含非单例的Bean
    // allowEagerInit：是否允许对”懒加载"的Bean进行实例化,这里主要针对FactoryBean，因为FactoryBean
    // 默认是懒加载的，为了推断它的类型可能会进行初始化。
	String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

	// 获取指定类型的Bean,返回一个map,key为bean的名称，value为对应的Bean
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException;
    
    // 获取指定类型的Bean,返回一个map,key为bean的名称，value为对应的Bean
    // includeNonSingletons：是否能包含非单例的Bean
    // allowEagerInit：是否允许对”懒加载"的Bean进行实例化,这里主要针对FactoryBean，因为FactoryBean
    // 默认是懒加载的，为了推断它的类型可能会进行初始化。
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException;

	// 获取添加了指定注解的Bean的名称
    // 为了确定类型，会对FactoryBean所创建的Bean进行实例化
	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

	// 获取添加了指定注解的Bean的名称
    // 为了确定类型，会对FactoryBean所创建的Bean进行实例化
    // 返回一个map,key为bean的名称，value为对应的Bean
	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	// 查询指定的Bean上的指定类型的注解，如果没有这个Bean会抛出NoSuchBeanDefinitionException
    // 如果指定Bean上不存在这个注解，会从其父类上查找
	@Nullable
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException;

}
12345678910111213141516171819202122232425262728293031323334353637383940414243444546474849505152
```

从上面的方法中可以看出，相对于BeanFactory，ListableBeanFactory提供了批量获取Bean的方法。

##### 3、AutowireCapableBeanFactory

```java
public interface AutowireCapableBeanFactory extends BeanFactory {
	
    // 自动注入下的四种模型，如果有疑问请参考之前的文章《自动注入与精确注入》
	int AUTOWIRE_NO = 0;
	int AUTOWIRE_BY_NAME = 1;
	int AUTOWIRE_BY_TYPE = 2;
	int AUTOWIRE_CONSTRUCTOR = 3;
	
    // 已经过时了，不考虑
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;
	
    //该属性是一种约定俗成的用法：以类全限定名+.ORIGINAL 作为Bean Name，用于告诉Spring，在初始化的时候，需要返回原始给定实例，而别返回代理对象
	String ORIGINAL_INSTANCE_SUFFIX = ".ORIGINAL";


	//-------------------------------------------------------------------------
	// 下面这三个方法通常用于创建跟填充Bean(对Bean进行属性注入)，但是请注意，直接采用下面这些方法创建或者装	  // 配的Bean不被Spring容器所管理
	//-------------------------------------------------------------------------
	
    // 用指定的class创建一个Bean,这个Bean会经过属性注入，并且会执行相关的后置处理器，但是并不会放入		// Spring容器中
	<T> T createBean(Class<T> beanClass) throws BeansException;
	
    // 为指定的一个对象完成属性注入,这个对象可以不被容器管理，可以是一个Spring容器外部的对象
    // 主要调用populateBean
	void autowireBean(Object existingBean) throws BeansException;
	
	// 配置参数中指定的bean
	// beanName表示在Bean定义中的名称。
	// populateBean和initializeBean都会被调用
    // existingBean：需要被配置的Bean
    // beanName：对应的Bean的名称
	Object configureBean(Object existingBean, String beanName) throws BeansException;


	//-------------------------------------------------------------------------
	// 下面这一系列方法主要为了更细粒度的操纵Bean的生命周期
	//-------------------------------------------------------------------------
    
    // 支持以给定的注入模型跟依赖检查级别创建，注入Bean。关于注入模型我这里就不想再说了
    // 依赖检查的级别如下：
    // 1.DEPENDENCY_CHECK_NONE = 0，代表不进行依赖检查
	// 2.DEPENDENCY_CHECK_SIMPLE = 2，代表对基本数据类的字段做检查。如果一个int类型的字段没有被赋值，那么会抛出异常
	// 3.DEPENDENCY_CHECK_ALL = 3，对引用类型的字段做检查。如果一个Object类型的字段没有被赋值，那么会抛出异常
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException;

    //就是把Bean定义信息里面的一些东西，赋值到已经存在的Bean里面
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;
	
    // 初始化Bean,执行初始化回调，及下面两个后置处理器中的方法
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

    // 调用对应的两个后置处理器
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException;
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException;
	
    // 执行销毁相关的回调方法
	void destroyBean(Object existingBean);


	//-------------------------------------------------------------------------
	// 关于注入点的相关方法
	//-------------------------------------------------------------------------
	
    // 查找唯一符合指定类的实例，如果有，则返回实例的名字和实例本身
	// 底层依赖于：BeanFactory中的getBean(Class)方法
	<T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException;
	
    // DependencyDescriptor:依赖名描述符，描述了依赖的相关情况，比如存在于哪个类，哪个字段，什么类型
    // 查找指定名称，指定类型的Bean
    // 底层依赖于：BeanFactory中的getBean(name,Class)方法
	Object resolveBeanByName(String name, DependencyDescriptor descriptor) throws BeansException;
	
    // 解析指定的依赖。就是根据依赖描述符的定义在容器中查找符合要求的Bean
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException;
	
	//descriptor 依赖描述 (field/method/constructor)
	//requestingBeanName 依赖描述所属的Bean
	//autowiredBeanNames 与指定Bean有依赖关系的Bean的名称
	//typeConverter 用以转换数组和连表的转换器
	//备注：结果可能为null，毕竟容器中可能不存在这个依赖嘛~~~~~~~~~~~~~~~~
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException;

}

123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293
```

可以看到这个类中的方法都跟装配Bean，配置Bean相关，另外还有一系列专门处理注入点的方法。可以看到接口有一个很大的作用就是对于一些不受Spring管理的Bean,也能为其提供依赖注入的功能。例如：

```java
// DmzService没有被放入容器中
public class DmzService {
	@Autowired
	IndexService indexService;

	public void test(){
		System.out.println(indexService);
	}
}

// 被容器所管理
@Component
public class IndexService {
}

public class Main {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(Config.class);
		AutowireCapableBeanFactory beanFactory = ac.getBeanFactory();
		DmzService bean = beanFactory.createBean(DmzService.class);
		// 打印：com.dmz.official.beanfactory.IndexService@6ad5c04e
		bean.test();
		// 抛出NoSuchBeanDefinitionException
		// ac.getBean(DmzService.class);
	}
}

123456789101112131415161718192021222324252627
```

在上面的例子中，`DmzService`没有被容器管理，所以在调用`ac.getBean(DmzService.class);`会抛出NoSuchBeanDefinitionException，但是我们可以看到，`indexService`被注入到了`DmzService`中。

##### 4、ConfigurableBeanFactory

```java
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {
	// 单例及原型的常量
	String SCOPE_SINGLETON = "singleton";
	String SCOPE_PROTOTYPE = "prototype";
	
    // 设置父容器，父容器一旦被设置，不可改变
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;
	
    // 为Bean设置指定的类加载器
	void setBeanClassLoader(@Nullable ClassLoader beanClassLoader);
	
    // 获取类型加载器，可能返回null,代表系统类加载器不可访问
	@Nullable
	ClassLoader getBeanClassLoader();
	
    // 设置临时的类加载器，在进行类加载时期织入时会用到（loadTimeWeaver）
	void setTempClassLoader(@Nullable ClassLoader tempClassLoader);
	@Nullable
	ClassLoader getTempClassLoader();
	
    // 是否缓存Bean的元数据，默认是开启的
	void setCacheBeanMetadata(boolean cacheBeanMetadata);
	boolean isCacheBeanMetadata();
	
    // 定义用于解析bean definition的表达式解析器
	void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver);
	@Nullable
	BeanExpressionResolver getBeanExpressionResolver();

    // 数据类型转换相关
	void setConversionService(@Nullable ConversionService conversionService);
	@Nullable
	ConversionService getConversionService();
	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);
	void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);
	void copyRegisteredEditorsTo(PropertyEditorRegistry registry);
	void setTypeConverter(TypeConverter typeConverter);
	TypeConverter getTypeConverter();
	
    // 值解析器，例如可以使用它来处理占位符
	void addEmbeddedValueResolver(StringValueResolver valueResolver);
	boolean hasEmbeddedValueResolver();
	@Nullable
	String resolveEmbeddedValue(String value);

    // 添加后置处理器
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
	int getBeanPostProcessorCount();

    // 注册指定名称的Scope
	void registerScope(String scopeName, Scope scope);
	
    // 返回所有的注册的scope的名称
	String[] getRegisteredScopeNames();
	
    // 返回指定名称的已注册的scope
	@Nullable
	Scope getRegisteredScope(String scopeName);
	
	AccessControlContext getAccessControlContext();
	
    // 从另外一个容器中拷贝配置，不包含具体的bean的定义
	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);
	
    // 为Bean注册别名
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;
	// 解析别名
	void resolveAliases(StringValueResolver valueResolver);
	
    // 合并BeanDefinition，参考我之前的文章，《BeanDefinition下》
	BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
	
    // 是否是一个FactoryBean
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

    // 循环依赖相关，标志一个Bean是否在创建中
	void setCurrentlyInCreation(String beanName, boolean inCreation);
	boolean isCurrentlyInCreation(String beanName);
	
	//处理bean依赖问题
	//注册一个依赖于指定bean的Bean
	void registerDependentBean(String beanName, String dependentBeanName);
	
    // 返回所有指定的Bean从属于哪些Bean
	String[] getDependentBeans(String beanName);
    
    // 返回指定名称的bean的所有依赖
	String[] getDependenciesForBean(String beanName);
    
    // 销毁Bean
	void destroyBean(String beanName, Object beanInstance);
	
    // 先从域中移除，然后再销毁
	void destroyScopedBean(String beanName);
	
    // 销毁所有单例
	void destroySingletons();

}
123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899
```

可以看到这个接口继承了`HierarchicalBeanFactory`，并基于它扩展了非常多的方法。除了继承了`HierarchicalBeanFactory`，还继承了一个`SingletonBeanRegistry`，其接口定义如下：

```java
public interface SingletonBeanRegistry {
	//以指定的名字将给定Object注册到BeanFactory中。
	//此接口相当于直接把Bean注册，所以都是准备好了的Bean。（动态的向容器里直接放置一个Bean）
	//什么BeanPostProcessor、InitializingBean、afterPropertiesSet等都不会被执行的，销毁的时候也不会收到destroy的信息
	void registerSingleton(String beanName, Object singletonObject);
	
    //以Object的形式返回指定名字的Bean，如果仅仅还是只有Bean定义信息，这里不会反悔
	// 需要注意的是：此方法不能直接通过别名获取Bean。若是别名，请通过BeanFactory的方法先获取到id
	@Nullable
	Object getSingleton(String beanName);
	//是否包含此单例Bean（不支持通过别名查找）
	boolean containsSingleton(String beanName);
	// 得到容器内所有的单例Bean的名字们
	String[] getSingletonNames();
	int getSingletonCount();
	
	// 获取当前这个注册表的互斥量(mutex),使用者通过该互斥量协同访问当前注册表
	Object getSingletonMutex();
}
12345678910111213141516171819
```

从上面可以看到，`SingletonBeanRegistry`主要是实现了对容器中单例池的管理。

##### 5、ConfigurableListableBeanFactory

```java
// 所有接口的集大成者，拥有上面所有接口的功能
public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {
	// 自动装配的模式下，忽略这个类型的依赖
	void ignoreDependencyType(Class<?> type);
	
    //自动装配的模式下，忽略这个接口类型的依赖
	void ignoreDependencyInterface(Class<?> ifc);

    // 注入一个指定类型的依赖。这个方法设计的目的主要是为了让容器中的Bean能依赖一个不被容器管理的Bean
	void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue);
	
    // 判断指定名称的Bean能否被注入到指定的依赖中
	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException;

    // 获取指定的BeanDefinition
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
	
    // 获取包含了所有的Bean的名称的迭代器
	Iterator<String> getBeanNamesIterator();

	// 清理元数据的缓存
	void clearMetadataCache();
	
	// 冻结所有的Bean配置
	void freezeConfiguration();
	boolean isConfigurationFrozen();
	
	// 实例化当前所有的剩下的单实例
	void preInstantiateSingletons() throws BeansException;
}
1234567891011121314151617181920212223242526272829303132
```

##### 6、AbstractBeanFactory

```java
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {
	//... 实现了大部分的方法，其中最终的实现为getBean()/doGetBean()方法的实现，提供了模版。其实createBean抽象方法，还是子类去实现的
	//... isSingleton(String name) / isPrototype(String name) / containsBean(String name) 也能实现精准的判断了

	// ===其中，它自己提供了三个抽象方法，子类必要去实现的===
	
	// 效果同：ListableBeanFactory#containsBeanDefinition  实现类：DefaultListableBeanFactory
	protected abstract boolean containsBeanDefinition(String beanName);
	// 效果同：ConfigurableListableBeanFactory#getBeanDefinition  实现类：DefaultListableBeanFactory
	protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;
	// 创建Bean的复杂逻辑，子类去实现。(子类：AbstractAutowireCapableBeanFactory)
	protected abstract Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException;
	
}

12345678910111213141516
```

##### 7、AbstractAutowireCapableBeanFactory

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {
    ......
        // 1.实现了AbstractBeanFactory中的createBean方法，能够创建一个完全的Bean
        // 2.实现了AutowireCapableBeanFactory，能对Bean进行实例化，属性注入，已经细粒度的生命周期管理
}

1234567
```

##### 8、DefaultListableBeanFactory

```java
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
    implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {
	.....
        // 没什么好说的了，最牛逼的一个BeanFactory，拥有上面的一切功能，额外的它实现了BeanDefinitionRegistry接口，具备注册管理BeanDefinition的功能
}

123456
```

### ApplicationContext体系汇总

ApplicationContext整体可以分为两个体系，一个就是web体系，另外一个就是非web体系。

#### 非web体系

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128114719385.png)

##### 1、ConfigurableApplicationContext

ApplicationContext接口中的方法比较简单，之前我们也一一分析它继承的接口以及它所具有的功能。并且ApplicationContext接口的方法都是只读的，不能对当前的容器做任何改变。而ConfigurableApplicationContext接口在ApplicationContext的基础上增加了很多进行配置的方法，比如添加事件监听器，添加后置处理器等等。

```java
public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle, Closeable {
	// 配置路径的分隔符
	String CONFIG_LOCATION_DELIMITERS = ",; \t\n";
	String CONVERSION_SERVICE_BEAN_NAME = "conversionService";
	String LOAD_TIME_WEAVER_BEAN_NAME = "loadTimeWeaver";
	String ENVIRONMENT_BEAN_NAME = "environment";
	String SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties";
	String SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment";

	//设置此应用程序上下文的唯一ID。
	void setId(String id);
	
    //设置父容器，设置后不能再改了
	void setParent(@Nullable ApplicationContext parent);
	
    //设置environment  此处为ConfigurableEnvironment 也是可以配置的应用上下文
	void setEnvironment(ConfigurableEnvironment environment);
	
    // 此处修改父类返回值为ConfigurableEnvironment 
	@Override
	ConfigurableEnvironment getEnvironment();

	//添加一个新的BeanFactoryPostProcessor（refresh()的时候会调用的）
	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor);
	
    // 添加一个事件监听器
	void addApplicationListener(ApplicationListener<?> listener);
	
    // 注册协议处理器  允许处理额外的资源协议
	void addProtocolResolver(ProtocolResolver resolver);

	//加载或刷新配置的持久表示  最最最重要的一个方法
	//表示可以是xml、可以是注解、可以是外部资源文件等等。。。。
	// 这个方法执行完成后，所有的单例Bean都已经被实例化，Bean工厂肯定也就被创建好了
	void refresh() throws BeansException, IllegalStateException;
	
	//JVM运行时注册一个关闭挂钩，在关闭JVM时关闭此上下文，除非此时已经关闭
	void registerShutdownHook();
	
	//关闭此应用程序上下文，释放实现可能持有的所有资源和锁  包括一些销毁、释放资源操作
	@Override
	void close();
	
    //标识上下文是否激活 refresh()后就会激活
	boolean isActive();
	
    // 返回此上下文内部的Bean工厂，可以用来访问底层工厂的特定功能。通过此工厂可以设置和验证所需的属性、自定义转换服务
	// 备注：父类方法为获得AutowireCapableBeanFactory接口，而此处的ConfigurableListableBeanFactory可配置、可列出Bean的工厂是它的子类
	ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;
}

123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051
```

##### 2、AbstractApplicationContext

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader
    implements ConfigurableApplicationContext {
	// 这个类实现了ConfigurableApplicationContext，具备了上面接口大部分功能，
    // 但是他没有实现getBeanFactory()方法，这个方法留待子类实现，所以它自己没有实际的管理Bean的能力，只是定义了一系列规范
}

123456
```

##### 3、AbstractRefreshableApplicationContext

```java
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {
	
    // 碰到重复的Bean时，是否允许覆盖原先的BeanDefinition
	@Nullable
	private Boolean allowBeanDefinitionOverriding;
	
    // 是否允许循环引用
	@Nullable
	private Boolean allowCircularReferences;
	
    // 默认持有一个DefaultListableBeanFactory
	@Nullable
	private DefaultListableBeanFactory beanFactory;

	// 对内部工厂进行操作时所采用的锁
	private final Object beanFactoryMonitor = new Object();

	public AbstractRefreshableApplicationContext() {
	}

	public AbstractRefreshableApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}

	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}
	
    // 刷新Bean工厂，如果当前上下文中已经存在一个容器的话，会先销毁容器中的所有Bean，然后关闭Bean工厂
    // 之后在重新创建一个DefaultListableBeanFactory
	@Override
	protected final void refreshBeanFactory() throws BeansException {
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			beanFactory.setSerializationId(getId());
			customizeBeanFactory(beanFactory);
			loadBeanDefinitions(beanFactory);
			synchronized (this.beanFactoryMonitor) {
				this.beanFactory = beanFactory;
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}

	@Override
	protected void cancelRefresh(BeansException ex) {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory != null) {
				this.beanFactory.setSerializationId(null);
			}
		}
		super.cancelRefresh(ex);
	}

	@Override
	protected final void closeBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory != null) {
				this.beanFactory.setSerializationId(null);
				this.beanFactory = null;
			}
		}
	}


	protected final boolean hasBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			return (this.beanFactory != null);
		}
	}
	
    // 复写了getBeanFactory，默认返回的是通过createBeanFactory创建的一个DefaultListableBeanFactory
	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("BeanFactory not initialized or already closed - " +
						"call 'refresh' before accessing beans via the ApplicationContext");
			}
			return this.beanFactory;
		}
	}

	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}

	.......
	// 提供了一个抽象的加载BeanDefinition的方法，这个方法没有具体实现，不同的配置方式需要进行不同的实现，
    // 到这里，配置的方式不能确定，既可能是以XML的方式，也可能是以java config的方式
    // 另外配置文件的加载方式也不能确定
	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory)
			throws BeansException, IOException;

}

123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106
```

可以看到这个类可以进一步对上下文进行配置，例如进行是否开启循环引用，是否允许进行BeanDefinition的覆盖等等。另外它所提供的一个重要的功能就是使容器具备刷新的功能，换言之凡是需要刷新功能的容器都需要继承这个类。

##### 4、AbstractRefreshableConfigApplicationContext

```java
public abstract class AbstractRefreshableConfigApplicationContext extends AbstractRefreshableApplicationContext
		implements BeanNameAware, InitializingBean {
	// 这个变量代表了配置文件的路径，到这里配置的信息相比于其父类AbstractRefreshableApplicationContext做了进一步的明确，但是仍然不能确定是XML还是javaconfig,只能确定配置在configLocations里面
	@Nullable
	private String[] configLocations;
    .....
}


123456789
```

##### 5、AbstractXmlApplicationContext

```java
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {
	
    
    // 是否进行XML类型的校验，默认为true
    private boolean validating = true;
    
    // .....
	
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {

		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		beanDefinitionReader.setEnvironment(this.getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}

	protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
		reader.setValidating(this.validating);
	}

	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			reader.loadBeanDefinitions(configResources);
		}
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			reader.loadBeanDefinitions(configLocations);
		}
	}

	@Nullable
	protected Resource[] getConfigResources() {
		return null;
	}

}

12345678910111213141516171819202122232425262728293031323334353637383940414243
```

可以看到这个类进一步对配置的加载做了进一步的明确，首先明确了配置的类型为XML，第二明确了要通过getConfigResources方法来加载需要的配置资源，但是并没有对这个方法做具体实现，因为对于Resource的定义，可能是通过classpath的方式，也可能是通过URL的方式，基于此又多了两个子类

1. `ClassPathXmlApplicationContext`，从classPath下加载配置文件
2. `FileSystemXmlApplicationContext`，基于URL的格式加载配置文件

##### 6、GenericApplicationContext

这个类已经不是抽象类了，我们可以直接使用它。但是这个类有一个很大的缺点，它不能读取配置，需要我们手动去指定读取的方式及位置。其实从上文中的分析我们可以看出，从AbstractApplicationContext到AbstractXmlApplicationContext一步步明确了配置的加载方式，Spring通过这种类的继承将配置的加载分了很多层，我们可以从AbstractXmlApplicationContext的子类开始从任意以及进行扩展。

而GenericApplicationContext只实现了上下文的基本功能，并没有对配置做任何约束，所以在使用它的我们需要手动往其中注册BeanDefinition。这样虽然很灵活，但是也很麻烦，如果我们使用GenericApplicationContext可能需要进行下面这样的操作

```java
GenericApplicationContext ctx = new GenericApplicationContext();
//使用XmlBeanDefinitionReader，这个地方我们甚至可以自己定义解析器，不使用Spring容器内部的
XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
//加载ClassPathResource
xmlReader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml"));
PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(ctx);
propReader.loadBeanDefinitions(new ClassPathResource("otherBeans.properties"));
//调用Refresh方法
ctx.refresh();

//和其他ApplicationContext方法一样的使用方式
MyBean myBean = (MyBean) ctx.getBean("myBean");

12345678910111213
```

平常开发中我们基本用不到这个东西

##### 7、AnnotationConfigApplicationContext

```java
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

	private final AnnotatedBeanDefinitionReader reader;

	private final ClassPathBeanDefinitionScanner scanner;
   
    .......
}

123456789
```

通过`AnnotatedBeanDefinitionReader`注册配置类，用`ClassPathBeanDefinitionScanner`扫描配置类上申明的路径，得到所有的BeanDefinition。然后其余的没啥了。这个我们经常使用，因为不用再需要xml文件了，使用`@Configuration`配置类即可，更加的方便。

#### web体系

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128113537858-20210128113556114.png)

##### 1、WebApplicationContext

```java
public interface WebApplicationContext extends ApplicationContext {

	String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

	String SCOPE_REQUEST = "request";

	String SCOPE_SESSION = "session";

	String SCOPE_APPLICATION = "application";

	String SERVLET_CONTEXT_BEAN_NAME = "servletContext";

	String CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters";

	String CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes";

	@Nullable
	ServletContext getServletContext();

}

123456789101112131415161718192021
```

定义了一堆常量，以及一个方法，约束了所有的web容器必须能返回一个Servlet的上下文（ServletContext）

##### 2、ConfigurableWebApplicationContext

```java
public interface ConfigurableWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

	String APPLICATION_CONTEXT_ID_PREFIX = WebApplicationContext.class.getName() + ":";

	String SERVLET_CONFIG_BEAN_NAME = "servletConfig";

	void setServletContext(@Nullable ServletContext servletContext);

	void setServletConfig(@Nullable ServletConfig servletConfig);

	@Nullable
	ServletConfig getServletConfig();
	
    // 设置及获取当前上下文的命名空间，命名空间用于区分不同的web容器的配置，在查找配置时会根据命名空间查找
    // 默认不进行命名空间配置，配置会在/WEB-INF/applicationContext.xml下查找
    // 如果配置了，会在/WEB-INF+"namespace"+/applicationContext.xml下查找
    // 根容器没有Namespace
	void setNamespace(@Nullable String namespace);
	@Nullable
	String getNamespace();

	void setConfigLocation(String configLocation);

	void setConfigLocations(String... configLocations);

	@Nullable
	String[] getConfigLocations();

}

123456789101112131415161718192021222324252627282930
```

可以看到使用这个类能指定上下文配置加载的位置

##### 3、AbstractRefreshableWebApplicationContext

```java
public abstract class AbstractRefreshableWebApplicationContext extends AbstractRefreshableConfigApplicationContext
		implements ConfigurableWebApplicationContext, ThemeSource {
    .......
}

12345
```

首先可以看到这个类继承了`AbstractRefreshableConfigApplicationContext`，代表它需要从指定的位置加载配置，其次它首先了ConfigurableWebApplicationContext，所以它具有web容器的属性。

##### 4、XmlWebApplicationContext

```java
public class XmlWebApplicationContext extends AbstractRefreshableWebApplicationContext {

    public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/applicationContext.xml";

    public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";


    public static final String DEFAULT_CONFIG_LOCATION_SUFFIX = ".xml";

	//  .......
    @Override
    protected String[] getDefaultConfigLocations() {
        if (getNamespace() != null) {
            return new String[] {DEFAULT_CONFIG_LOCATION_PREFIX + getNamespace() + DEFAULT_CONFIG_LOCATION_SUFFIX};
        }
        else {
            return new String[] {DEFAULT_CONFIG_LOCATION};
        }
    }

}

12345678910111213141516171819202122
```

进一步指定了配置文件的加载形式

1. 需要加载XML类型配置
2. 对于根容器，加载路径为`/WEB-INF/applicationContext.xml`
3. 对于子容器，加载路径为`/WEB-INF/+'namespace'+.xml`,比如常用的dispatchServlet.xml

##### 5、AnnotationConfigWebApplicationContext

指定了以注解的方式配置web容器

##### 6、GenericWebApplicationContext

类比`GenericApplicationContext`,没有指定配置相关的任何东西，全手动

### 总结

从上面我们可以看到，整个一套体系下来不可谓不庞大，Spring在单一职责可以说做到了极致。不论是按功能分，比如`HierarchicalBeanFactory`,`ListableBeanFactory`,`AutowireCapableBeanFactory`就是按照不同功能拆分，或者是按照功能实现的层级划分，比如上面说到的配置文件的加载机制。对类之间的关系进行明确的分层，代表了整个体系会具备非常强大的扩展性，我们可以在每一步进行自己的扩展。这是让Spring能组件化开发，可插拔，变得如此优秀、普适的重要原因

到此，关于ApplicationContext相关的内容终于也可以告一段落了，代表着IOC已经结束了，粗略看了下官网，接下来还剩数据绑定，数据校验，类型转换以及AOP，任重而道远，加油吧！~

## Spring官网阅读（十四）Spring中的BeanWrapper及类型转换

> BeanWrapper是Spring中一个很重要的接口，Spring在通过配置信息创建对象时，第一步首先就是创建一个BeanWrapper。这篇文章我们就分析下这个接口，本文内容主要对应官网中的`3.3`及`3.4`小结

### 接口定义

```java
// Spring低级JavaBeans基础设施的中央接口。通常来说并不直接使用BeanWrapper，而是借助BeanFactory或者DataBinder来一起使用,BeanWrapper对Spring中的Bean做了包装，为的是更加方便的操作Bean中的属性
public interface BeanWrapper extends ConfigurablePropertyAccessor {
	
	void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);
	int getAutoGrowCollectionLimit();

	// 获取包装的Bean
	Object getWrappedInstance();

	// 获取包装的Bean的class
	Class<?> getWrappedClass();

	// 获取所有属性的属性描述符
	PropertyDescriptor[] getPropertyDescriptors();

	// 获取指定属性的属性描述符
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException;

}
```

这里需要解释一个概念，什么是属性描述符？

> PropertyDescriptor：属性描述符，能够描述javaBean中的属性，通过属性描述符我们能知道这个属性的类型，获取到操纵属性的方法（getter/setter）

### 继承关系

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128155236337.png)

BeanWrapper的子类只有一个：`BeanWrapperImpl`,它继承了`ConfigurablePropertyAccessor`，这个接口的主要功能是进行属性访问，同时它又有三个父接口，接下来我们一一分析他们的功能。

### 接口功能

#### 1、PropertyEditorRegistry（属性编辑器注册器）

##### 接口定义

```java
// 这个接口的功能很简单，就是用来注入属性编辑器（PropertyEditor），那么什么是PropertyEditor呢？
public interface PropertyEditorRegistry {

	void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);

	void registerCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath, PropertyEditor propertyEditor);

	@Nullable
	PropertyEditor findCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath);

}
```

##### PropertyEditor

概念

> **PropertyEditor是JavaBean规范定义的接口**，这是`java.beans`中一个接口，**其设计的意图是图形化编程上**，方便对象与String之间的转换工作，而Spring将其扩展，方便各种对象与String之间的转换工作。

###### Spring中对PropertyEditor使用的实例

1. 我们在通过XML的方式对Spring中的Bean进行配置时，不管Bean中的属性是何种类型，都是直接通过字面值来设置Bean中的属性。那么是什么在这其中做转换呢？这里用到的就是PropertyEditor
2. SpringMVC在解析请求参数时，也是使用的PropertyEditor

###### Spring内置的PropertyEditor

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200331082212430.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

#### 2、PropertyAccessor（属性访问器）

##### 接口定义

```java
public interface PropertyAccessor {
	
    // 嵌套属性的分隔符,比如"foo.bar"将会调用getFoo().getBar()两个方法
	String NESTED_PROPERTY_SEPARATOR = ".";
	char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

   // 代表角标index的符号  如person.addresses[0]  这样就可以把值放进集合/数组/Map里了
	String PROPERTY_KEY_PREFIX = "[";
	char PROPERTY_KEY_PREFIX_CHAR = '[';
	String PROPERTY_KEY_SUFFIX = "]";
	char PROPERTY_KEY_SUFFIX_CHAR = ']';

    // 该属性是否可读/可写，不存在则返回false
	boolean isReadableProperty(String propertyName);
	boolean isWritableProperty(String propertyName);
	
    // 获取/设置属性的方法，基本见名知意
	@Nullable
	Class<?> getPropertyType(String propertyName) throws BeansException;
	@Nullable
	TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException;
	@Nullable
	Object getPropertyValue(String propertyName) throws BeansException;
	void setPropertyValue(String propertyName, @Nullable Object value) throws BeansException;
	void setPropertyValue(PropertyValue pv) throws BeansException;
	void setPropertyValues(Map<?, ?> map) throws BeansException;
	void setPropertyValues(PropertyValues pvs) throws BeansException;
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown)
			throws BeansException;
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
			throws BeansException;

}
```

这里需要解释一个概念，什么是PropertyValue？

> 当设置属性值时，少不了两样东西：
>
> 1. 属性访问表达式：如`listMap[0][0]`
> 2. 属性值：
>
> `ProperyValue`对象就是用来封装这些信息的。如果某个值要给赋值给bean属性，Spring都会把这个值包装成`ProperyValue`对象。

#### 3、TypeConverter（类型转换器）

##### 接口定义

```java
// 定义了进行类型转换时的一些规范，就像名字定义的那样，主要用来做类型转换
public interface TypeConverter {
	
    // 将指定的值转换成指定的类型
	@Nullable
	<T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType) throws TypeMismatchException;

    // 相对于上面这个方法下面这个三种方法能处理转换过程中的泛型
	@Nullable
	<T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable MethodParameter methodParam) throws TypeMismatchException;
	@Nullable
	<T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType, @Nullable Field field)
			throws TypeMismatchException;
	default <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable TypeDescriptor typeDescriptor) throws TypeMismatchException {

		throw new UnsupportedOperationException("TypeDescriptor resolution not supported");
	}

}
```

#### 4、ConfigurablePropertyAccessor

```java
public interface ConfigurablePropertyAccessor extends PropertyAccessor, PropertyEditorRegistry, TypeConverter {
	// ConversionService：进行转换的业务类，转换系统的入口
	void setConversionService(@Nullable ConversionService conversionService);
	@Nullable
	ConversionService getConversionService();
    
    // 进行属性编辑是是否返回旧的值
	void setExtractOldValueForEditor(boolean extractOldValueForEditor);
	boolean isExtractOldValueForEditor();
	
    // 当设置（dog.name）这种嵌套属性的情况下，如果dog属性为null是否会报错
    // 为true的话不会，为false会抛出NullValueInNestedPathException
	void setAutoGrowNestedPaths(boolean autoGrowNestedPaths);
	boolean isAutoGrowNestedPaths();

}
```

**从上面可以看到，BeanWrapper接口自身对Bean进行了一层包装**。**另外它的几个通过间接继承了几个接口，所以它还能对Bean中的属性进行操作。PropertyAccessor赋予了BeanWrapper对属性进行访问及设置的能力，在对Bean中属性进行设置时，不可避免的需要对类型进行转换，而恰好PropertyEditorRegistry，TypeConverter就提供了类型转换的统一约束。**

在了解了接口之后，我们接下来看看它的唯一实现类`BeanWrapperImpl`

### 唯一子类（BeanWrapperImpl）

#### 继承关系

![在这里插入图片描述](/Users/weiliang/Desktop/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128113454890.png)

结合我们之前对接口的分析以及上面这张UML图，我们可以知道BeanWrapperImpl主要实现了一下几个功能

1. 对Bean进行包装
2. 对Bean的属性进行访问以及设置
3. 在操作属性的过程中，必然涉及到类型转换，所以还有类型转换的功能

#### Java中的内置机制

> 在详细了解BeanWrapperImpl前，必须要了解java中的一个机制：**内省**

##### 核心概念

 首先可以先了解下JavaBean的概念：一种特殊的类，主要用于传递数据信息。这种类中的方法主要用于访问私有的字段，且方法名符合某种命名规则。如果在两个模块之间传递信息，可以将信息封装进JavaBean中，这种对象称为“值对象”(Value Object)，或“VO”。

因此JavaBean都有如下几个特征：

1. 属性都是私有的；
2. 有无参的public构造方法；
3. 对私有属性根据需要提供公有的getXxx方法以及setXxx方法；
4. getters必须有返回值没有方法参数；setter值没有返回值，有方法参数；

符合这些特征的类，被称为JavaBean；JDK中提供了一套API用来访问某个属性的getter/setter方法，这些API存放在java.beans中，这就是内省(Introspector)。

**内省和反射的区别:**

> 反射：Java反射机制是在运行中，对任意一个类，能够获取得到这个类的所有属性和方法；它针对的是任意类
> 内省（Introspector）：是Java语言对JavaBean类属性、事件的处理方法

1. 反射可以操作各种类的属性，而内省只是通过反射来操作JavaBean的属性
2. 内省设置属性值肯定会调用setter方法，反射可以不用（反射可直接操作属性Field）
3. 反射就像照镜子，然后能看到.class的所有，是客观的事实。内省更像主观的判断：比如**看到getName()，内省就会认为这个类中有name字段，但事实上并不一定会有name**；通过内省可以获取bean的getter/setter

##### 使用示例

```java
public class Main {
    public static void main(String[] args) throws Exception{
        BeanInfo beanInfo = Introspector.getBeanInfo(People.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            System.out.print(propertyDescriptor.getName()+"   ");
        }
    }
    // 程序输出：age   class   name 
    // 为什么会输出class呢？前文中有提到，“看到getName()，内省就会认为这个类中有name字段，但事实上并不一定会有name”，
    // 我们知道每个对象都会有getClass方法，所以使用内省时，默认就认为它具有class这个字段
}

class People{
    String name;
    int age;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
}
```

#### 源码分析

```java
// 这个类我只保留一些关键的代码，其余的琐碎代码都不看了
public class BeanWrapperImpl extends AbstractNestablePropertyAccessor implements BeanWrapper {
	// 缓存内省的结果，BeanWrapperImpl就是通过这个对象来完成对包装的Bean的属性的控制
	@Nullable
	private CachedIntrospectionResults cachedIntrospectionResults;
    ......       
   	public void setBeanInstance(Object object) {
		this.wrappedObject = object;
		this.rootObject = object;
        // 实际进行类型转换的对象：typeConverterDelegate
		this.typeConverterDelegate = new TypeConverterDelegate(this, this.wrappedObject);
		setIntrospectionClass(object.getClass());
	}
    ......
    // 最终调用的就是CachedIntrospectionResults的forClass方法进行内省并缓存，底层调用的就是java的内省机制
    private CachedIntrospectionResults getCachedIntrospectionResults() {
        if (this.cachedIntrospectionResults == null) {
            this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(getWrappedClass());
        }
        return this.cachedIntrospectionResults;
    }
   .......
       // 最终进行类型转换的方法
       private Object convertIfNecessary(@Nullable String propertyName, @Nullable Object oldValue,
                                         @Nullable Object newValue, @Nullable Class<?> requiredType, @Nullable TypeDescriptor td)
       throws TypeMismatchException {

       Assert.state(this.typeConverterDelegate != null, "No TypeConverterDelegate");
       try {
           // 可以看到，最后就是调用typeConverterDelegate来进行类型转换
           return this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue, newValue, requiredType, td);
       }
       ......
   }
}
```

#### 父类作用分析

对于接口，我们已经分析过了，这里就不再赘述了，我们重点看下BeanWrapperImpl继承的几个父类

##### PropertyEditorRegistrySupport

这个类最大的作用在于管理`PropertyEditor`,添加了很多的默认的`PropertyEditor`。在`PropertyEditorRegistry`的基础上做了进一步的扩展，提供的还是属性编辑器注册的功能。

##### TypeConverterSupport

```java
public abstract class TypeConverterSupport extends PropertyEditorRegistrySupport implements TypeConverter {
  @Nullable
	TypeConverterDelegate typeConverterDelegate;
......
}
```

这个接口实现了TypeConverter，所以它具有类型转换的能力，而它这种能力的实现，依赖于它所持有的一个TypeConverterDelegate。

##### AbstractPropertyAccessor

```java
public abstract class AbstractPropertyAccessor extends TypeConverterSupport implements ConfigurablePropertyAccessor {
	// 省略部分代码......
	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
			throws BeansException {

		List<PropertyAccessException> propertyAccessExceptions = null;
		List<PropertyValue> propertyValues = (pvs instanceof MutablePropertyValues ?
				((MutablePropertyValues) pvs).getPropertyValueList() : Arrays.asList(pvs.getPropertyValues()));
		for (PropertyValue pv : propertyValues) {
			try {
				setPropertyValue(pv);
			}
			// ....
		}
	}

	@Override
	@Nullable
	public abstract Object getPropertyValue(String propertyName) throws BeansException;

	@Override
	public abstract void setPropertyValue(String propertyName, @Nullable Object value) throws BeansException;

}
```

核心的代码其实就是这些，这个类继承了`TypeConverterSupport`,所以它具备了类型转换的能力。同时它也是一个属性访问器，但是它只是实现了批量设置属性的方法，真正的`setPropertyValue`还是留待子类实现。可以看到，到这个类为止，还没有将属性的设置跟类型转换的能力结合起来。

##### AbstractNestablePropertyAccessor

这个类开始真正的将属性访问跟类型转换结合到一起，它真正的实现了`setPropertyValue`，并在设置属性的时候会进行类型的转换，具体代码就不看了，非常繁杂，但是整体不难。

上面我们多次提到了类型转换，但是还没有真正看到类型转换的逻辑，因为上面类最终将类型转换的逻辑委托给了`TypeConverterDelegate`。接下来我们看看，类型转换到底是怎么完成。

### 类型转换

#### TypeConverterDelegate

这个类我们只看一个核心方法，如下：

```java
class TypeConverterDelegate {

	private final PropertyEditorRegistrySupport propertyEditorRegistry;

	@Nullable
	private final Object targetObject;

	public <T> T convertIfNecessary(@Nullable String propertyName, @Nullable Object oldValue, @Nullable Object newValue,
			@Nullable Class<T> requiredType, @Nullable TypeDescriptor typeDescriptor) throws IllegalArgumentException {

		// 查看是否为当前这个类型配置了定制的PropertyEditor
		PropertyEditor editor = this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);

		ConversionFailedException conversionAttemptEx = null;

		// 获取当前容器中的类型转换业务类
		ConversionService conversionService = this.propertyEditorRegistry.getConversionService();
        
        // 在这里可以看出，Spring底层在进行类型转换时有两套机制
        // 1.首选的是采用PropertyEditor
        // 2.在没有配置PropertyEditor的情况下，会采用conversionService
		if (editor == null && conversionService != null && newValue != null && typeDescriptor != null) {
			TypeDescriptor sourceTypeDesc = TypeDescriptor.forObject(newValue);
			if (conversionService.canConvert(sourceTypeDesc, typeDescriptor)) {
				try {
                    // 通过conversionService进行类型转换
					return (T) conversionService.convert(newValue, sourceTypeDesc, typeDescriptor);
				}
				catch (ConversionFailedException ex) {
					// fallback to default conversion logic below
					conversionAttemptEx = ex;
				}
			}
		}

		Object convertedValue = newValue;

		// 配置了定制的属性编辑器，采用PropertyEditor进行属性转换
		if (editor != null || (requiredType != null && !ClassUtils.isAssignableValue(requiredType, convertedValue))) {
			if (typeDescriptor != null && requiredType != null && Collection.class.isAssignableFrom(requiredType) &&
					convertedValue instanceof String) {
				TypeDescriptor elementTypeDesc = typeDescriptor.getElementTypeDescriptor();
				if (elementTypeDesc != null) {
					Class<?> elementType = elementTypeDesc.getType();
					if (Class.class == elementType || Enum.class.isAssignableFrom(elementType)) {
						convertedValue = StringUtils.commaDelimitedListToStringArray((String) convertedValue);
					}
				}
			}
			if (editor == null) {
                // 没有配置定制的属性编辑器，采用默认的属性编辑器
				editor = findDefaultEditor(requiredType);
			}
            // 采用属性编辑器进行转换，需要注意的是，默认情况下PropertyEditor只会对String类型的值进行类型转换
			convertedValue = doConvertValue(oldValue, convertedValue, requiredType, editor);
		}
        // .....
		return (T) convertedValue;
	}
	
}
```

从上面的代码中我们可以知道，Spring在实现类型转换时，有两套机制，第一套机制依赖于PropertyEditor，第二套机制依赖于ConversionService。关于属性编辑器PropertyEditor我们之前已经介绍过了，主要进行的是String到Object的转换，正因为如此，属性编辑器进行类型转换有很大的局限性，所以Spring又推出了一套ConversionService的体系。

#### ConversionService体系

##### ConversionService相关接口

###### 1、Converter

- ##### 接口定义

```java
package org.springframework.core.convert.converter;

// 将一个S类型的数据转换成T类型
public interface Converter<S, T> {

    T convert(S source);
}
1234567
```

这个接口只能进行一对一的转换，S->T

###### 2、ConverterFactory

- ##### 接口定义

```java
public interface ConverterFactory<S, R> {
	
    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}
1234
```

利用这个转换工厂，我们可以进行一对多的转换，以Spring内置的一个转换器为例：

```java
final class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

	@Override
	public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToEnum(ConversionUtils.getEnumType(targetType));
	}


	private class StringToEnum<T extends Enum> implements Converter<String, T> {

		private final Class<T> enumType;

		public StringToEnum(Class<T> enumType) {
			this.enumType = enumType;
		}

		@Override
		public T convert(String source) {
			if (source.isEmpty()) {
				// It's an empty enum identifier: reset the enum value to null.
				return null;
			}
			return (T) Enum.valueOf(this.enumType, source.trim());
		}
	}

}
```

通过传入不同的枚举类型，我们可以从这个工厂中获取到不同的转换器，并把对应的String类型的参数转换成对应的枚举类型数据。

可以看到，通过ConverterFactory，我们能实现一对多的类型转换S->(T extends R)

###### 3、GenericConverter

- #### 接口定义

```java
public interface GenericConverter {
	
    // 获取能够转换的ConvertiblePair的集合，这个对象就是一组可以进行转换的类型
	@Nullable
	Set<ConvertiblePair> getConvertibleTypes();
	
    // 根据源数据类型转换成目标类型数据
	@Nullable
	Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType);

	final class ConvertiblePair {
		// 源数据类型
		private final Class<?> sourceType;
		// 目标数据类型
		private final Class<?> targetType;

		// .....省略部分代码
	}

}
```

相比于前面的Converter以及ConverterFactory，这个接口就更加牛逼了，使用它能完成多对多的转换。因为它内部保存了一个能够进行转换的ConvertiblePair的集合，每个ConvertiblePair代表一组能进行转换的数据类型。同时，这个接口相比我们前面介绍的两个接口，更加的复杂，所以一般情况也不推荐使用这个接口，没有非常必要的话，最好是使用上面两种

一般GenericConverter会与ConditionalGenericConverter配合使用，其接口定义如下：

```java
public interface ConditionalConverter {
	// 判断是否需要对目标类型转换到原类型，返回true的话代表要执行转换，否则不执行转换
    boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType);
}

// 结合了上面两个接口
public interface ConditionalGenericConverter extends GenericConverter, ConditionalConverter {
}
```

我们来看下Spring内部的一个实际使用的例子：

```java
final class StringToCollectionConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return (targetType.getElementTypeDescriptor() == null ||
                // 根据conversionService来判断是否需要执行转换
				this.conversionService.canConvert(sourceType, targetType.getElementTypeDescriptor()));
	}

	@Override
	@Nullable
	public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			// 这里会借助conversionService来执行转换
	}

}
```

可以看到，最终的实现还是借助了ConversionService，那么ConversionService到底是啥呢？

##### ConversionService总体结构

- ##### 接口定义

```java
public interface ConversionService {
	
  // 判断是否能进行类型转换
	boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);
	boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType);
	
  // 进行类型转换
	@Nullable
	<T> T convert(@Nullable Object source, Class<T> targetType);
	@Nullable
	Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType);

}
```

###### UML类图

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128113341906.png)

一般来说，实现了`ConversionService`跟`ConverterRegistry`会结合使用，对于这种`xxxRegistry`我相信大家猜都能猜出来它是干什么的了，代码如下：

- ##### ConverterRegistry

```java
// 就是在添加Converter或者ConverterFactory
public interface ConverterRegistry {
	
	void addConverter(Converter<?, ?> converter);

	<S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter);

	void addConverter(GenericConverter converter);

	void addConverterFactory(ConverterFactory<?, ?> factory);

	void removeConvertible(Class<?> sourceType, Class<?> targetType);

}
1234567891011121314
```

- ##### ConfigurableConversionService

```java
// 单纯的整合了ConversionService以及ConverterRegistry的功能
public interface ConfigurableConversionService extends ConversionService, ConverterRegistry {

}
1234
```

- ##### GenericConversionService

这个类已经是一个具体的实现类，可以直接使用，但是我们一般不会直接使用它，而是使用它的子类`DefaultConversionService`,因为子类提供了很多默认的转换器。

- ##### DefaultConversionService

```java
public class DefaultConversionService extends GenericConversionService {

	@Nullable
	private static volatile DefaultConversionService sharedInstance;

	public DefaultConversionService() {
		addDefaultConverters(this);
	}

	public static ConversionService getSharedInstance() {
		DefaultConversionService cs = sharedInstance;
		if (cs == null) {
			synchronized (DefaultConversionService.class) {
				cs = sharedInstance;
				if (cs == null) {
					cs = new DefaultConversionService();
					sharedInstance = cs;
				}
			}
		}
		return cs;
	}

	public static void addDefaultConverters(ConverterRegistry converterRegistry) {
		addScalarConverters(converterRegistry);
		addCollectionConverters(converterRegistry);

		converterRegistry.addConverter(new ByteBufferConverter((ConversionService) converterRegistry));
		......
	}

	public static void addCollectionConverters(ConverterRegistry converterRegistry) {
		......
	}

	private static void addScalarConverters(ConverterRegistry converterRegistry) {
		converterRegistry.addConverterFactory(new NumberToNumberConverterFactory());
		......
	}

}

123456789101112131415161718192021222324252627282930313233343536373839404142
```

相比其父类`GenericConversionService`，这个子类默认添加了很多的转换器，这样可以极大的方便我们进行开发，所以一般情况下我们都会使用这个类。

##### ConversionService如何配置

讲了这么多，那么如何往容器中配置一个ConversionService呢？我们需要借助Spring提供的一个`ConversionServiceFactoryBean`。其代码如下：

```java
public class ConversionServiceFactoryBean implements FactoryBean<ConversionService>, InitializingBean {

	@Nullable
	private Set<?> converters;

	@Nullable
	private GenericConversionService conversionService;

	public void setConverters(Set<?> converters) {
		this.converters = converters;
	}

	@Override
	public void afterPropertiesSet() {
		this.conversionService = createConversionService();
		ConversionServiceFactory.registerConverters(this.converters, this.conversionService);
	}
	
	protected GenericConversionService createConversionService() {
		return new DefaultConversionService();
	}

	@Override
	@Nullable
	public ConversionService getObject() {
		return this.conversionService;
	}

	@Override
	public Class<? extends ConversionService> getObjectType() {
		return GenericConversionService.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
```

这个类的实现逻辑很简单，`ConversionServiceFactoryBean`创建完成后，在进行初始化时调用`afterPropertiesSet`方法，创建一个`DefaultConversionService`，然后将提供的`converters`全部注册到这个`DefaultConversionService`中。所以我们进行如下的配置就行了

```xml
<bean id="conversionService" class="org.springframework.context.support.ConversionServiceFactoryBean">
    <property name="converters">
        <set>
            # 提供自己的converter,可以覆盖默认的配置
            <bean class="example.MyCustomConverter"/>
        </set>
    </property>
</bean>
123456789
```

### 总结

这篇文章中，我们学习了BeanWrapper，知道一个BeanWrapper其实就是一个Bean的包装器，它对Bean包装的目的是为了能操纵Bean中的属性，所以它同时需要具备获取以及设置Bean中的属性能力，所以它也必须是一个**属性访问器**（`PropertyAccessor`），另外为了将各种不同类型的配置数据绑定到Bean的属性上，那么它还得具备属性转换的能力，因为它还得是一个**类型转换器**（`TypeConverter`）。

通过上面的分析，我们知道Spring中将类型转换的功能都委托给了一个`TypeConverterDelegate`，这个委托类在进行类型转换时会有两套方案：

1. PropertyEditor，这是Spring最初提供的方案，扩展了java中的PropertyEditor（java原先提供这个接口的目的更多是为了进行图形化编程）
2. ConversionService，Spring后来提供的一个进行类型转换的体系，用来取代PropertyEditor，因为PropertyEditor有很大的局限性，只能进行String->Object的转换。

画图如下：

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/mPSCkaT6qzoMplN-20210128174846171.png)

## Spring官网阅读（十五）Spring中的格式化（Formatter）

> 在上篇文章中，我们已经学习过了Spring中的类型转换机制。现在我们考虑这样一个需求：在我们web应用中，我们经常需要将前端传入的字符串类型的数据转换成指定格式或者指定数据类型来满足我们调用需求，同样的，后端开发也需要将返回数据调整成指定格式或者指定类型返回到前端页面。这种情况下，Converter已经没法直接支撑我们的需求了。这个时候，格式化的作用就很明显了，这篇文章我们就来介绍Spring中格式化的一套体系。本文主要涉及官网中的`3.5`及`3.6`小结

### Formatter

#### 接口定义

```java
public interface Formatter<T> extends Printer<T>, Parser<T> {
}
```

可以看到，本身这个接口没有定义任何方法，只是聚合了另外两个接口的功能

- Printer

```java
// 将T类型的数据根据Locale信息打印成指定格式，即返回字符串的格式
public interface Printer<T> {
    String print(T fieldValue, Locale locale);
}
```

- Parser

```java
public interface Parser<T> {
	// 将指定的字符串根据Locale信息转换成指定的T类型数据
    T parse(String clientValue, Locale locale) throws ParseException;
}
```

从上面可以看出，这个两个接口维护了两个功能相反的方法，分别完成对String类型数据的解析以及格式化。

#### 继承树

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200401085240704.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

可以发现整个继承关系并不复杂，甚至可以说非常简单。只有一个抽象子类，`AbstractNumberFormatter`，这个类抽象了对数字进行格式化时的一些方法，它有三个子类，分别处理不同的数字类型，包括`货币`，`百分数`,`正常数字`。其余的子类都是直接实现了`Formatter`接口。其中我们比较熟悉的可能就是`DateFormatter`了

使用如下：

```java
public class Main {
	public static void main(String[] args) throws Exception {
		DateFormatter dateFormatter = new DateFormatter();
		dateFormatter.setIso(DateTimeFormat.ISO.DATE);
		System.out.println(dateFormatter.print(new Date(), Locale.CHINA));
		System.out.println(dateFormatter.parse("2020-03-26", Locale.CHINA));
        // 程序打印：
        // 2020-03-26
		// Thu Mar 26 08:00:00 CST 2020
	}
}
```

### AnnotationFormatterFactory注解驱动的格式化

我们在配置格式化时，除了根据类型进行格式外（比如常见的根据Date类型进行格式化），还可以根据注解来进行格式化，最常见的注解就是`org.springframework.format.annotation.DateTimeFormat`。除此之外还有`NumberFormat`，它们都在format包下。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200401085251962.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

为了将一个注解绑定到指定的格式化器上，我们需要借助到一个接口`AnnotationFormatterFactory`

AnnotationFormatterFactory

```java
public interface AnnotationFormatterFactory<A extends Annotation> {
	// 可能被添加注解的字段的类型
	Set<Class<?>> getFieldTypes();
	 
    // 根据注解及字段类型获取一个格式化器
	Printer<?> getPrinter(A annotation, Class<?> fieldType);
	
    // 根据注解及字段类型获取一个解析器
	Parser<?> getParser(A annotation, Class<?> fieldType);

}
```

以Spring内置的一个`DateTimeFormatAnnotationFormatterFactory`来说，这个类实现的功能就是将`DateTimeFormat`注解绑定到指定的格式化器，源码如下：

```java
public class DateTimeFormatAnnotationFormatterFactory  extends EmbeddedValueResolutionSupport
		implements AnnotationFormatterFactory<DateTimeFormat> {

	private static final Set<Class<?>> FIELD_TYPES;
	
    // 只有在这些类型下加这个注解才会进行格式化
	static {
		Set<Class<?>> fieldTypes = new HashSet<>(4);
		fieldTypes.add(Date.class);
		fieldTypes.add(Calendar.class);
		fieldTypes.add(Long.class);
		FIELD_TYPES = Collections.unmodifiableSet(fieldTypes);
	}


	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(DateTimeFormat annotation, Class<?> fieldType) {
		return getFormatter(annotation, fieldType);
	}

	@Override
	public Parser<?> getParser(DateTimeFormat annotation, Class<?> fieldType) {
		return getFormatter(annotation, fieldType);
	}
	
	protected Formatter<Date> getFormatter(DateTimeFormat annotation, Class<?> fieldType) {			// 通过这个DateFormatter来完成格式化
		DateFormatter formatter = new DateFormatter();
		String style = resolveEmbeddedValue(annotation.style());
		if (StringUtils.hasLength(style)) {
			formatter.setStylePattern(style);
		}
		formatter.setIso(annotation.iso());
		String pattern = resolveEmbeddedValue(annotation.pattern());
		if (StringUtils.hasLength(pattern)) {
			formatter.setPattern(pattern);
		}
		return formatter;
	}

}
```

使用`@DateTimeFormat`,我们只需要在字段上添加即可

```java
public class MyModel {
    @DateTimeFormat(iso=ISO.DATE)
    private Date date;
}
```

关于日期的格式化，Spring还提供了一个类似的`AnnotationFormatterFactory`，专门用于处理java8中的日期格式，如下

```java
public class Jsr310DateTimeFormatAnnotationFormatterFactory extends EmbeddedValueResolutionSupport
    implements AnnotationFormatterFactory<DateTimeFormat> {

    private static final Set<Class<?>> FIELD_TYPES;

    static {
        // 这里添加了对Java8日期的支持
        Set<Class<?>> fieldTypes = new HashSet<>(8);
        fieldTypes.add(LocalDate.class);
        fieldTypes.add(LocalTime.class);
        fieldTypes.add(LocalDateTime.class);
        fieldTypes.add(ZonedDateTime.class);
        fieldTypes.add(OffsetDateTime.class);
        fieldTypes.add(OffsetTime.class);
        FIELD_TYPES = Collections.unmodifiableSet(fieldTypes);
    }
    ........
```

学习到现在，对Spring的脾气大家应该都有所了解，上面这些都是定义了具体的功能实现，它们必定会有一个管理者，一个`Registry`，用来注册这些格式化器

### FormatterRegistry

#### 接口定义

```java
// 继承了ConverterRegistry，所以它同时还是一个Converter注册器
public interface FormatterRegistry extends ConverterRegistry {
	
    // 一系列添加格式化器的方法
    void addFormatterForFieldType(Class<?> fieldType, Printer<?> printer, Parser<?> parser);
    void addFormatterForFieldType(Class<?> fieldType, Formatter<?> formatter);
    void addFormatterForFieldType(Formatter<?> formatter);
    void addFormatterForAnnotation(AnnotationFormatterFactory<?, ?> factory);
}
123456789
```

#### UML类图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200401085302216.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

我们可以发现`FormatterRegistry`默认只有两个实现类

##### FormattingConversionService

```java
// 继承了GenericConversionService ，所以它能对Converter进行一系列的操作
// 实现了接口FormatterRegistry，所以它也可以注册格式化器了
// 实现了EmbeddedValueResolverAware，所以它还能有非常强大的功能：处理占位符
public class FormattingConversionService extends GenericConversionService implements FormatterRegistry, EmbeddedValueResolverAware {
	// ....
   
  
	// 最终也是交给addFormatterForFieldType去做的
	// getFieldType：它会拿到泛型类型。并且支持DecoratingProxy
	@Override
	public void addFormatter(Formatter<?> formatter) {
		addFormatterForFieldType(getFieldType(formatter), formatter);
	}
	// 存储都是分开存储的  读写分离
	// PrinterConverter和ParserConverter都是一个GenericConverter  采用内部类实现的
	// 注意：他们的ConvertiblePair必有一个类型是String.class
	// Locale一般都可以这么获取：LocaleContextHolder.getLocale()
    // 在进行printer之前，会先判断是否能进行类型转换，如果能进行类型转换会先进行类型转换，之后再格式化
	// 在parse之后，会判断是否还需要进行类型转换，如果需要类型转换会先进行类型转换
	@Override
	public void addFormatterForFieldType(Class<?> fieldType, Formatter<?> formatter) {
		addConverter(new PrinterConverter(fieldType, formatter, this));
		addConverter(new ParserConverter(fieldType, formatter, this));
	}


	// 哪怕你是一个AnnotationFormatterFactory，最终也是被适配成了GenericConverter（ConditionalGenericConverter）
	@Override
	public void addFormatterForFieldAnnotation(AnnotationFormatterFactory<? extends Annotation> annotationFormatterFactory) {
		Class<? extends Annotation> annotationType = getAnnotationType(annotationFormatterFactory);

		// 若你自定义的实现了EmbeddedValueResolverAware接口，还可以使用占位符哟
		// AnnotationFormatterFactory是下面的重点内容
		if (this.embeddedValueResolver != null && annotationFormatterFactory instanceof EmbeddedValueResolverAware) {
			((EmbeddedValueResolverAware) annotationFormatterFactory).setEmbeddedValueResolver(this.embeddedValueResolver);
		}
		
		// 对每一种字段的type  都注册一个AnnotationPrinterConverter去处理
		// AnnotationPrinterConverter是一个ConditionalGenericConverter
		// matches方法为：sourceType.hasAnnotation(this.annotationType);
		// 这个判断是呼应的：因为annotationFormatterFactory只会作用在指定的字段类型上的，不符合类型条件的不用添加
		Set<Class<?>> fieldTypes = annotationFormatterFactory.getFieldTypes();
		for (Class<?> fieldType : fieldTypes) {
			addConverter(new AnnotationPrinterConverter(annotationType, annotationFormatterFactory, fieldType));
			addConverter(new AnnotationParserConverter(annotationType, annotationFormatterFactory, fieldType));
		}
	}
	// .......
    
    // 持有的一个内部类
    private static class PrinterConverter implements GenericConverter {

        private final Class<?> fieldType;

        private final TypeDescriptor printerObjectType;

        @SuppressWarnings("rawtypes")
        private final Printer printer;
		
        // 最终也是通过conversionService完成类型转换
        private final ConversionService conversionService;

        public PrinterConverter(Class<?> fieldType, Printer<?> printer, ConversionService conversionService) {
            this.fieldType = fieldType;
            this.printerObjectType = 
                // 会通过解析Printer中的泛型获取具体类型，主要是为了判断是否需要进行类型转换
                TypeDescriptor.valueOf(resolvePrinterObjectType(printer));
            this.printer = printer;
            this.conversionService = conversionService;
        }
	// ......
    
}
```

##### DefaultFormattingConversionService

类比我们上篇文中介绍的`GenericConversionService`跟`DefaultConversionService`，它相比于`FormattingConversionService`而言，提供了大量的默认的格式化器，源码如下：

```java
public class DefaultFormattingConversionService extends FormattingConversionService {

	private static final boolean jsr354Present;

	private static final boolean jodaTimePresent;

	static {
		ClassLoader classLoader = DefaultFormattingConversionService.class.getClassLoader();
        // 判断是否导入了jsr354相关的包
		jsr354Present = ClassUtils.isPresent("javax.money.MonetaryAmount", classLoader);
        // 判断是否导入了joda
		jodaTimePresent = ClassUtils.isPresent("org.joda.time.LocalDate", classLoader);
	}
	
    // 会注册很多默认的格式化器
	public DefaultFormattingConversionService() {
		this(null, true);
	}
	public DefaultFormattingConversionService(boolean registerDefaultFormatters) {
		this(null, registerDefaultFormatters);
	}

	public DefaultFormattingConversionService(
			@Nullable StringValueResolver embeddedValueResolver, boolean registerDefaultFormatters) {

		if (embeddedValueResolver != null) {
			setEmbeddedValueResolver(embeddedValueResolver);
		}
		DefaultConversionService.addDefaultConverters(this);
		if (registerDefaultFormatters) {
			addDefaultFormatters(this);
		}
	}
    
	public static void addDefaultFormatters(FormatterRegistry formatterRegistry) {
		// 添加针对@NumberFormat的格式化器
		formatterRegistry.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());

		// 针对货币的格式化器
		if (jsr354Present) {
			formatterRegistry.addFormatter(new CurrencyUnitFormatter());
			formatterRegistry.addFormatter(new MonetaryAmountFormatter());
			formatterRegistry.addFormatterForFieldAnnotation(new Jsr354NumberFormatAnnotationFormatterFactory());
		}
		new DateTimeFormatterRegistrar().registerFormatters(formatterRegistry);
        
        // 如没有导入joda的包，那就默认使用Date
		if (jodaTimePresent) {
			// 针对Joda
			new JodaTimeFormatterRegistrar().registerFormatters(formatterRegistry);
		}
		else {
            // 没有joda的包，是否Date
			new DateFormatterRegistrar().registerFormatters(formatterRegistry);
		}
	}

}
```

##### FormatterRegistrar

在上面`DefaultFormattingConversionService`的源码中，有这么几行：

```java
new JodaTimeFormatterRegistrar().registerFormatters(formatterRegistry);
new DateFormatterRegistrar().registerFormatters(formatterRegistry);
```

其中的`JodaTimeFormatterRegistrar`，`DateFormatterRegistrar`就是`FormatterRegistrar`。那么这个接口有什么用呢？我们先来看看它的接口定义：

```java
public interface FormatterRegistrar {
	// 最终也是调用FormatterRegistry来完成注册
    void registerFormatters(FormatterRegistry registry);
}
```

我们思考一个问题，为什么已经有了`FormatterRegistry`,Spring还要开发一个`FormatterRegistrar`呢？直接使用`FormatterRegistry`完成注册不好吗？

以这句代码为例：`new JodaTimeFormatterRegistrar().registerFormatters(formatterRegistry)`,这段代码是将`joda`包下所有的默认的转换器已经注册器都注册到`formatterRegistry`中。

我们可以发现`FormatterRegistrar`相当于对格式化器及转换器进行了分组，我们调用它的`registerFormatters`方法，相当于将这一组格式化器直接添加到指定的`formatterRegistry`中。这样做的好处在于，如果我们对同一个类型的数据有两组不同的格式化策略，例如就以上面的日期为例，我们既有可能采用`joda`的策略进行格式化，也有可能采用`Date`的策略进行格式化，通过分组的方式，我们可以更见方便的在确认好策略后将需要的格式化器添加到容器中。

### 配置SpringMVC中的格式化器

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
	
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 调用registry.addFormatter添加格式化器即可
    }
}
```

配置实现的原理

1、`@EnableWebMvc`注解上导入了一个`DelegatingWebMvcConfiguration`类

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}
```

2、`DelegatingWebMvcConfiguration`

```java
// 继承了WebMvcConfigurationSupport
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

	private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();
	
    // 这个方法会注入所有的WebMvcConfigurer，包括我们的WebConfig
	@Autowired(required = false)
	public void setConfigurers(List<WebMvcConfigurer> configurers) {
		if (!CollectionUtils.isEmpty(configurers)) {
			this.configurers.addWebMvcConfigurers(configurers);
		}
	}

	//.....,省略无关代码
    
    // 复写了父类WebMvcConfigurationSupport的方法
    // 调用我们配置的configurer的addFormatters方法
	@Override
	protected void addFormatters(FormatterRegistry registry) {
		this.configurers.addFormatters(registry);
	}	
   //.....,省略无关代码
}
```

3、`WebMvcConfigurationSupport`

```java
public class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {
	
  // 这就是真相，这里会创建一个FormattingConversionService，并且是一个DefaultFormattingConversionService，然后调用addFormatters方法
	@Bean
	public FormattingConversionService mvcConversionService() {
		FormattingConversionService conversionService = new DefaultFormattingConversionService();
		addFormatters(conversionService);
		return conversionService;
	}
	protected void addFormatters(FormatterRegistry registry) {
	}
}
```

### 总结

Spring中的格式化到此就结束了，总结画图如下：

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128121232924.png)



## Spring官网阅读（十六）Spring中的数据绑定

> 在前面的文章我们学习过了Spring中的类型转换以及格式化，对于这两个功能一个很重要的应用场景就是应用于我们在XML中配置的Bean的属性值上，如下：
>
> ```xml
> <bean class="com.dmz.official.converter.service.IndexService" name="indexService">
> 		<property name="name" value="dmz"/>
>      <!-- age 为int类型-->
> 		<property name="age" value="1"/>
> </bean>
> ```
> 
>在上面这种情况下，我们从XML中解析出来的值类型肯定是String类型，而对象中的属性为int类型，当Spring将配置中的数据应用到Bean上时，就调用了我们的类型转换器完成了String类型的字面值到int类型的转换。
> 
>那么除了在上面这种情况中使用了类型转换，还有哪些地方用到了呢？对了，就是本文要介绍的数据绑定–`DataBinder`。

### DataBinder

#### UML类图

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/20200405205433834-20210128181152716.png)

从上图我们可以看到，`DataBinder`实现了`PropertyEditorRegistry`以及`TypeConverter`，所以它拥有类型转换的能力。

我们通过下面两张图对比下`BeanWrapperImpl`跟`DataBinder`

1. `DataBinder`

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128181209020.png)

1. `BeanWrapperImpl`

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128181224419.png)
可以发现跟`BeanWrapperImpl`不同的是，它并没有通过继承某一个类来实现类型转换，而是通过组合的方式（`DataBinder`持有一个`SimpleTypeConverter`的引用，通过这个`SimpleTypeConverter`完成了类型转换）

#### 使用示例

```java
public class Main {
	public static void main(String[] args) throws BindException {
		Person person = new Person();
		DataBinder binder = new DataBinder(person, "person");
    // 创建用于绑定到对象上的属性对（属性名称，属性值）
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "fsx");
		pvs.add("age", 18);
		binder.bind(pvs);
		System.out.println(person);
    // 程序打印：Person{name='dmz', age=18}
	}
}

class Person {
	String name;
	int age;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "Person{" +
				"name='" + name + '\'' +
				", age=" + age +
				'}';
	}
}
```

> 在上面的例子中要明确一点，Person中必须要提供setter方法（getter方法可以不提供，因为我们只是设置值），实际上`DataBinder`底层也是同样也是采用了Java的**内省机制**（关于Java的内省机制如果不了解的话，请参考《Spring官网阅读十四》），而内省只会根据setter方法以及getter来设置或者获取Bean中的属性。

#### 源码分析

可能有细心的同学会发现，`DataBinder`是位于我们的`org.springframework.validation`包下的，也就是说它跟Spring中的校验也有关系，不过校验相关的内容不是我们本节要探讨的，本文我们只探讨`DataBinder`跟数据绑定相关的内容。

`DataBinder`所在的包结构如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200405205458600.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

OK，明确了要分析的点之后，接下来我们就看看到底数据是如何绑定到我们的对象上去的，核心代码如下：

##### bind方法

第一步，我们是直接调用了bind方法来完成，其代码如下：

```java
public void bind(PropertyValues pvs) {
    MutablePropertyValues mpvs = (pvs instanceof MutablePropertyValues ?
                                  (MutablePropertyValues) pvs : new MutablePropertyValues(pvs));
    // 最终调用了doBind方法，如果大家对Spring代码有所了解的话，会发现Spring中有很多doXXX的方法
    // 形如doXXX这种命名方式的方法往往就是真正“干活”的代码，对于本例来说，肯定就是它来完成数据绑定的
    doBind(mpvs);
}
```

##### doBind方法

```java
protected void doBind(MutablePropertyValues mpvs) {
    // 校验
    checkAllowedFields(mpvs);
    // 校验
    checkRequiredFields(mpvs);
    // 真正进行数据绑定
    applyPropertyValues(mpvs);
}
```

跟校验相关的代码不在本文的探讨范围内，如果感兴趣的话可以关注我接下来的文章。我们现在把注意力放在`applyPropertyValues`这个方法，方法名直译过来的意思是--------应用属性值，就是将方法参数中的属性值应用到Bean上，也就是进行属性绑定。不知道大家看到这个方法名是否熟悉，如果对源码有一定了解的话，一定会知道Spring在完成属性注入的过程中调用了一个同名的方法，关于这个方法稍后我会带大家找一找然后做个比较，现在我们先看看`doBind`方法中`applyPropertyValues`干了什么

##### applyPropertyValues方法

```java
protected void applyPropertyValues(MutablePropertyValues mpvs) {
    try {
        // 逻辑非常简单，获取一个属性访问器，然后直接通过属性访问器将属性值设置上去
        // IgnoreUnknownFields：忽略在Bean中找不到的属性
        // IgnoreInvalidFields：忽略找到，但是没有访问权限的值
        getPropertyAccessor().setPropertyValues(mpvs, isIgnoreUnknownFields(), isIgnoreInvalidFields());
    }
    catch (PropertyBatchUpdateException ex) {
        // 省略部分代码.....
    }
}
```

这段代码主要做了两件事

###### 获取一个属性访问器

`getPropertyAccessor()`,获取一个属性访问器，关于属性访问器在《Spring官网阅读十四》也有介绍，这里我再做一些补充

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200405205504507.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

可以看到，`PropertyAccessor`（也就是我们所说的属性访问器）只有两个实现类

- 第一个，`BeanWrapperImpl`
- 第二个，`DirectFieldAccessor`

那么这两个有什么区别呢？第一个我们已经知道了，它是基于内省来实现的，所以`BeanWrapperImpl`肯定是基于getter,setter方法来实现对属性的操作的。第二个从名字上我们可以猜测，它估计是直接通过反射来获取字段的，也就是说，不需要提供setter/getter方法。大家可以自行做个测试，这里我就直接给结论了

- `BeanWrapperImpl`，基于内省，依赖getter/setter方法
- `DirectFieldAccessor`，基于反射，不需要提供getter/setter方法

那么接下来，我们思考一个问题，`DataBinder`中的`getPropertyAccessor()`访问的是哪种类型的属性访问器呢？其实结合我们之前那个使用的示例就很容易知道，它肯定返回的是一个基于内省机制实现的属性访问器，并且它就是返回了一个`BeanWrapperImpl`。代码如下：

```java
// 1.获取一个属性访问器，可以看到，是通过getInternalBindingResult()方法返回的一个对象来获取的
// 那么getInternalBindingResult()做了什么呢？
protected ConfigurablePropertyAccessor getPropertyAccessor() {
    return getInternalBindingResult().getPropertyAccessor();
}

// 2.getInternalBindingResult()又调用了一个initBeanPropertyAccess()，从名字上来看，就是用来初始化属性访问器的，再看看这个方法干了啥
protected AbstractPropertyBindingResult getInternalBindingResult() {
    if (this.bindingResult == null) {
        initBeanPropertyAccess();
    }
    return this.bindingResult;
}

// 3.调用了一个createBeanPropertyBindingResult，创建了一个对象，也就是通过创建的这个对象返回了一个属性访问器，那么这个对象是什么呢？接着往下看
public void initBeanPropertyAccess() {
    Assert.state(this.bindingResult == null,
                 "DataBinder is already initialized - call initBeanPropertyAccess before other configuration methods");
    this.bindingResult = createBeanPropertyBindingResult();
}

// 4.可以发现创建的这个对象就是一个BeanPropertyBindingResult
protected AbstractPropertyBindingResult createBeanPropertyBindingResult() {
    BeanPropertyBindingResult result = new BeanPropertyBindingResult(getTarget(),
                                                                     getObjectName(), isAutoGrowNestedPaths(), getAutoGrowCollectionLimit());
    // .....
    return result;
}

// 5.跟踪这个对象的getPropertyAccessor()方法，发现就是返回了一个beanWrapper
// 现在明朗了吧，dataBinder最终也是依赖于beanWrapper
public final ConfigurablePropertyAccessor getPropertyAccessor() {
    if (this.beanWrapper == null) {
        this.beanWrapper = createBeanWrapper();
        this.beanWrapper.setExtractOldValueForEditor(true);
        this.beanWrapper.setAutoGrowNestedPaths(this.autoGrowNestedPaths);
        this.beanWrapper.setAutoGrowCollectionLimit(this.autoGrowCollectionLimit);
    }
    return this.beanWrapper;
}
```

> 我们可以思考一个问题，为什么Spring在实现数据绑定的时候不采用`DirectFieldAccessor`而是`BeanWrapperImpl`呢？换言之，为什么不直接使用反射而使用内省呢？
>
> 我个人的理解是：反射容易打破Bean的封装性，基于内省更安全。Spring在很多地方都不推荐使用反射的方式，比如我们在使用@Autowired注解进行字段注入的时候，编译器也会提示，”Field injection is not recommended “，不推荐我们使用字段注入，最好将@Autowired添加到setter方法上。

###### 通过属性访问器直接set属性值

> 这段代码十分繁琐，如果不感兴趣可以直接跳过，整个核心就是获取到对象中的setter方法，然后反射调用。

- 1、setPropertyValues

此方法位于`org.springframework.beans.AbstractPropertyAccessor#setPropertyValues(org.springframework.beans.PropertyValues, boolean, boolean)`

```java
public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
    throws BeansException {

    List<PropertyAccessException> propertyAccessExceptions = null;
    List<PropertyValue> propertyValues = (pvs instanceof MutablePropertyValues ?
                                          ((MutablePropertyValues) pvs).getPropertyValueList() : Arrays.asList(pvs.getPropertyValues()));
    for (PropertyValue pv : propertyValues) {
        try {
            // 核心代码就是这一句
            setPropertyValue(pv);
        }
		// ......
    }
```

- 2、setPropertyValue（String,Object）

此方法位于`org.springframework.beans.AbstractNestablePropertyAccessor#setPropertyValue(java.lang.String, java.lang.Object)`

```java
public void setPropertyValue(String propertyName, @Nullable Object value) throws BeansException {
    AbstractNestablePropertyAccessor nestedPa;
    try {
        // 这里是为了解决嵌套属性的情况，比如一个person对象中，包含一个dog对象，dog对象中有一个name属性
        // 那么我们可以通过dog.name这种方式来将一个名字直接绑定到person中的dog上
        // 与此同时，我们不能再使用person的属性访问器了，因为使用dog的属性访问器，这里就是返回dog的属性访问器
        nestedPa = getPropertyAccessorForPropertyPath(propertyName);
    }
    // .......
    
    // PropertyTokenHolder是什么呢？例如我们的Person对象中有一个List<String> name的属性，
    // 那么我们在绑定时，需要对List中的元素进行赋值，所有我们会使用name[0],name[1]这种方式来进行绑定，
    // 而PropertyTokenHolder中有三个属性，其中actualName代表name,canonicalName代表整个表达式name[0],而key则代表0这个下标位置
    PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
    // 最后通过属性访问器设置值
    nestedPa.setPropertyValue(tokens, new PropertyValue(propertyName, value));
}
```

对上面的结论进行测试，测试代码如下：

```java
public class Main {
	public static void main(String[] args) throws BindException {
		Person person = new Person();
		DataBinder binder = new DataBinder(person, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("dog.dogName","dawang");
    pvs.add("name[0]", "dmz0");
		pvs.add("name[1]", "dmz1");
		pvs.add("age", 18);
		binder.bind(pvs);
		System.out.println(person);
	}
}

class Dog {
  // 省略getter/setter方法
	String dogName;
}

class Person {
  // 省略getter/setter方法
	List<String> name;
	Dog dog;
	int age;
}
```

在方法的如下位置添加条件断点（`propertyName.equals("dog.dogName")`）：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200405205510354.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

启动main方法，并开始调试，程序进入如下结果：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200405205518201.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

我们关注红框标注的三个位置

1. 第一个红框，标注了当前属性访问器所对应的对象为Dog
2. 第二个红框，这是一个特殊的`AbstractNestablePropertyAccessor`,专门用于处理嵌套属性这种情况的，所以它包含了嵌套的路径
3. 第三个红框，标注了这个嵌套的属性访问器的根对象是Person

同样的，按照这种方式我们也可以对Person中的`List<String> name`属性进行调试，可以发现`PropertyTokenHolder`就是按照上文所说的方式进行存储数据的，大家可以自行调试，我这里就不在演示了。

- 3、setPropertyValue（PropertyTokenHolder，PropertyValue）

这个方法是对上面方法的重载，其代码仍然位于`org.springframework.beans.AbstractNestablePropertyAccessor`中，代码如下：

```java
protected void setPropertyValue(PropertyTokenHolder tokens, PropertyValue pv) throws BeansException {
    if (tokens.keys != null) {
        // 前面已经说过了，keys其实就是下标数组，如果你能看到这里的话，肯定会有一个疑问,为什么需要一个数组呢？考虑这种属性List<List<String>> list,这个时候为了表示它,是不是就要list[0][0]这种方式了呢？这个时候就需要用数组存储了，因为一个属性需要多个下标表示
        processKeyedProperty(tokens, pv);
    }
    else {
        // 我们关注这个方法即可，解析完PropertyTokenHolder后，最终都要调用这个方法
        processLocalProperty(tokens, pv);
    }
}

1234567891011
```

- 4、processLocalProperty

代码位于：`org.springframework.beans.AbstractNestablePropertyAccessor#processLocalProperty`

```java
	private void processLocalProperty(PropertyTokenHolder tokens, PropertyValue pv) {
		PropertyHandler ph = getLocalPropertyHandler(tokens.actualName);
		// .... 省略部分代码
		Object oldValue = null;
		try {
			Object originalValue = pv.getValue();
			Object valueToApply = originalValue;
            // 判断成立，代表需要进行类型转换，conversionNecessary为null或者为true都成立
			if (!Boolean.FALSE.equals(pv.conversionNecessary)) {
                // 判断成立，代表已经转换过了
				if (pv.isConverted()) {
					valueToApply = pv.getConvertedValue();
				}
				else {
					if (isExtractOldValueForEditor() && ph.isReadable()) {
						try {
							oldValue = ph.getValue();
						}
		// .... 省略部分代码
					}
                    // 类型转换的部分，之前已经分析过了，这里就没什么好讲的了
					valueToApply = convertForProperty(
							tokens.canonicalName, oldValue, originalValue, ph.toTypeDescriptor());
				}
				pv.getOriginalPropertyValue().conversionNecessary = (valueToApply != originalValue);
			}
            // 核心代码就这一句
			ph.setValue(valueToApply);
		}
	// .... 省略部分代码
		}
	}

123456789101112131415161718192021222324252627282930313233
```

- 5、setValue

代码位置：`org.springframework.beans.BeanWrapperImpl.BeanPropertyHandler#setValue`

最终进入到`BeanWrapperImpl`中的一个内部类`BeanPropertyHandler`中，方法代码如下:

```java
public void setValue(final @Nullable Object value) throws Exception {
    final Method writeMethod = (this.pd instanceof GenericTypeAwarePropertyDescriptor ?
                                ((GenericTypeAwarePropertyDescriptor) this.pd).getWriteMethodForActualAccess() :
                                this.pd.getWriteMethod());
 	// .... 省略部分代码
        ReflectionUtils.makeAccessible(writeMethod);
        writeMethod.invoke(getWrappedInstance(), value);
    }
}

12345678910
```

代码就是这么的简单，内省获取这个属性的`writeMethod`，其实就是`setter`方法，然后直接反射调用

------

在了解了`DataBinder`之后，我们再来学习跟基于`DataBinder`实现的子类

### DataBinder的子类

#### 子类概览

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200405205524468.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

可以看到`DataBinder`的直接子类只有一个`WebDataBinder`，从名字上我们就能知道，这个类主要作用于Web环境，从而也说明了数据绑定主要使用在Web环境中。

##### WebDataBinder

> 这个接口是为了Web环境而设计的，但是并不依赖任何的`Servlet API`。它主要的作用是作为一个基类让其它的类继承，例如`ServletRequestDataBinder`。

###### 代码分析

```java
public class WebDataBinder extends DataBinder {
	
    // 这两个字段的详细作用见下面的两个方法checkFieldDefaults/checkFieldMarkers
	public static final String DEFAULT_FIELD_MARKER_PREFIX = "_";
	public static final String DEFAULT_FIELD_DEFAULT_PREFIX = "!";
	@Nullable
	private String fieldMarkerPrefix = DEFAULT_FIELD_MARKER_PREFIX;
	@Nullable
	private String fieldDefaultPrefix = DEFAULT_FIELD_DEFAULT_PREFIX;

    // ......省略构造方法及一些getter/setter方法

	@Override
	protected void doBind(MutablePropertyValues mpvs) {
		checkFieldDefaults(mpvs);
		checkFieldMarkers(mpvs);
        // 没有对数据绑定做什么扩展，只是单纯的调用了父类的方法，也就是DataBinder的方法
		super.doBind(mpvs);
	}
	

    // 若你给定的PropertyValue的属性名是以!开头的，例如，传入的属性名称为：!name,属性值为：dmz
    // 那就做处理如下：
    // 如果Bean中的name属性是可写的并且mpvs不存在name属性，那么向mpvs中添加一个属性对，其中属性名称为name,值为dmz
    // 然后将!name这个属性值对从mpvs中移除
    // 其实这里就是说你可以使用！来给个默认值。比如!name表示若找不到name这个属性的时，就取它的值，
    // 也就是说你request里若有穿!name保底，也就不怕出现null值啦
	protected void checkFieldDefaults(MutablePropertyValues mpvs) {
		String fieldDefaultPrefix = getFieldDefaultPrefix();
		if (fieldDefaultPrefix != null) {
			PropertyValue[] pvArray = mpvs.getPropertyValues();
			for (PropertyValue pv : pvArray) {
				if (pv.getName().startsWith(fieldDefaultPrefix)) {
					String field = pv.getName().substring(fieldDefaultPrefix.length());
                    // 属性可写，并且当前要绑定的属性值中不包含这个去除了“!”的属性名
					if (getPropertyAccessor().isWritableProperty(field) && !mpvs.contains(field)) {
                        // 添加到要绑定到Bean中的属性值集合里
						mpvs.add(field, pv.getValue());
					}
					mpvs.removePropertyValue(pv);
				}
			}
		}
	}
    
    // 处理_的步骤
    // 若传入的字段以“_”开头，以属性名称：“_name”，属性值dmz为例
    // 如果Bean中的name字段可写，并且mpvs没有这个值
    // 那么对Bean中的name字段赋默认的空值，比如Boolean类型默认给false，数组给空数组[]，集合给空集合，Map给空map 
    // 然后移除mpvs中的“_name”
    // 相当于说，当我们进行数据绑定时，传入“_name”时，如果没有传入具体的属性值，Spring会为我们赋默认的空值
    // 前提是必须以“_”开头
	protected void checkFieldMarkers(MutablePropertyValues mpvs) {
		String fieldMarkerPrefix = getFieldMarkerPrefix();
		if (fieldMarkerPrefix != null) {
			PropertyValue[] pvArray = mpvs.getPropertyValues();
			for (PropertyValue pv : pvArray) {
				if (pv.getName().startsWith(fieldMarkerPrefix)) {
					String field = pv.getName().substring(fieldMarkerPrefix.length());
					if (getPropertyAccessor().isWritableProperty(field) && !mpvs.contains(field)) {
						Class<?> fieldType = getPropertyAccessor().getPropertyType(field);
						mpvs.add(field, getEmptyValue(field, fieldType));
					}
					mpvs.removePropertyValue(pv);
				}
			}
		}
	}

	@Nullable
	protected Object getEmptyValue(String field, @Nullable Class<?> fieldType) {
		return (fieldType != null ? getEmptyValue(fieldType) : null);
	}
	
    // 根据不同的类型给出空值
	@Nullable
	public Object getEmptyValue(Class<?> fieldType) {
		try {
           // 布尔值，默认false
			if (boolean.class == fieldType || Boolean.class == fieldType) {
				return Boolean.FALSE;
			}
            // 数组，默认给一个长度为0的符合要求的类型的数组 
			else if (fieldType.isArray()) {
				return Array.newInstance(fieldType.getComponentType(), 0);
			}
            // 集合，也是给各种空集合，Set/List等等
			else if (Collection.class.isAssignableFrom(fieldType)) {
				return CollectionFactory.createCollection(fieldType, 0);
			}
			else if (Map.class.isAssignableFrom(fieldType)) {
				return CollectionFactory.createMap(fieldType, 0);
			}
		}
		catch (IllegalArgumentException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to create default value - falling back to null: " + ex.getMessage());
			}
		}
		// Default value: null.
		return null;
	}
	
    // 这个方法表示，支持将文件作为属性绑定到对象的上
	protected void bindMultipart(Map<String, List<MultipartFile>> multipartFiles, MutablePropertyValues mpvs) {
		multipartFiles.forEach((key, values) -> {
			if (values.size() == 1) {
				MultipartFile value = values.get(0);
				if (isBindEmptyMultipartFiles() || !value.isEmpty()) {
					mpvs.add(key, value);
				}
			}
			else {
				mpvs.add(key, values);
			}
		});
	}

}

123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120
```

可以看到相对于父类`DataBinder`，它主要做了以下三点增强

1. 可以手动为Bean中的属性提供默认值（提供“!”开头的属性名称）
2. 可以让容器对属性字段赋上某些空值（提供“_”开头的属性名称）
3. 可以将文件绑定到Bean上

###### 使用示例

```java
public class WebDataBinderMain {
	public static void main(String[] args) {
		A  a = new A();
		WebDataBinder webDataBinder = new WebDataBinder(a);
		MutablePropertyValues propertyValues = new MutablePropertyValues();
		// propertyValues.add("name","I AM dmz");
		propertyValues.add("!name","dmz");
		propertyValues.add("_list","10");
		webDataBinder.bind(propertyValues);
		System.out.println(a);
        // 程序打印：
       // A{name='dmz', age=0, multipartFile=null, list=[], no_list=null}
       // 如果注释打开，程序打印：A{name='I AM dmz', age=0, multipartFile=null, list=[], no_list=null}
	}
}

// 省略getter/setter方法
class A{
	String name;
	int age;
	MultipartFile multipartFile;
	List<String> list;
	List<String> no_list;
	}
}

1234567891011121314151617181920212223242526
```

##### ServletRequestDataBinder

> 相比于父类，明确的依赖了`Servlet API`，会从`ServletRequest`中解析出参数，然后绑定到对应的Bean上，同时还能将文件对象绑定到Bean上。

###### 代码分析

```java
public class ServletRequestDataBinder extends WebDataBinder {

	public void bind(ServletRequest request) {
        // 从request中解析除MutablePropertyValues，用于后面的数据绑定
		MutablePropertyValues mpvs = new ServletRequestParameterPropertyValues(request);
		
        // 如果是一个MultipartRequest，返回一个MultipartRequest
        // 上传文件时，都是使用MultipartRequest来封装请求
        MultipartRequest multipartRequest = WebUtils.getNativeRequest(request, MultipartRequest.class);
        
        // 说明这个请求对象是一个MultipartRequest
		if (multipartRequest != null) {
            
            // 调用父类方法绑定对应的文件
			bindMultipart(multipartRequest.getMultiFileMap(), mpvs);
		}
        // 留给子类扩展使用
		addBindValues(mpvs, request);
        
        // 调用WebDataBinder的doBind方法进行数据绑定
		doBind(mpvs);
	}

	protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
	}
	//....... 省略部分代码

}

1234567891011121314151617181920212223242526272829
```

##### ExtendedServletRequestDataBinder

###### 代码分析

```java
public class ExtendedServletRequestDataBinder extends ServletRequestDataBinder {
	// ....省略构造方法
   
    // 这个类在ServletRequestDataBinder复写了addBindValues方法，在上面我们说过了，本身这个方法也是ServletRequestDataBinder专门提供了用于子类复写的方法
    @Override
    @SuppressWarnings("unchecked")
    protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
        String attr = HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
        // 它会从request获取名为HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE的属性
        // 我们在使用@PathVariable的时候，解析出来的参数就放在request中的这个属性上，然后由ExtendedServletRequestDataBinder完成数据绑定
        Map<String, String> uriVars = (Map<String, String>) request.getAttribute(attr);
        if (uriVars != null) {
            uriVars.forEach((name, value) -> {
                if (mpvs.contains(name)) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Skipping URI variable '" + name +
                                    "' because request contains bind value with same name.");
                    }
                }
                else {
                    mpvs.addPropertyValue(name, value);
                }
            });
        }
    }

}

12345678910111213141516171819202122232425262728
```

##### WebExchangeDataBinder

> 这个绑定器用于web-flux响应式编程中，用于完成Mono类型的数据的绑定，最终绑定的动作还是调用的父类的doBind方法

##### MapDataBinder

> 它位于`org.springframework.data.web`是和Spring-Data相关，专门用于处理`target`是`Map`类型的目标对象的绑定，它并非一个public类，Spring定义的用于内部使用的类

##### WebRequestDataBinder

> 它是用于处理Spring自己定义的`org.springframework.web.context.request.WebRequest`的，旨在处理和容器无关的web请求数据绑定

### 总结

上面关于Web相关的数据绑定我没有做详细的介绍，毕竟当前的学习阶段的重点是针对Spring-Framework，对于Web相关的东西目前主要以了解为主，后续在完成SpringMVC相关文章时会对这部分做详细的介绍。

本文主要介绍了DataBinder的整个体系，重点学习了它的数据绑定相关的知识，但是不要忘记了，它本身也可以实现类型转换的功能。实际上，我们也可以这样理解，之所以要让DataBinder具备类型转换的能力，正是为了更好的完成数据绑定。

前文我们也提到了，DataBinder位于`org.springframework.validation`，所以它必定跟校验有关，具体有什么关系呢？下篇文章将详细介绍及分析Spring中的数据校验，它也将是整个SpringFramwork官网阅读笔记的最后一篇文章！

## Spring官网阅读（十七）Spring中的数据校验

> 在前文中我们一起学习了Spring中的数据绑定，也就是整个`DataBinder`的体系，其中有提到`DataBinder`跟校验相关。可能对于Spring中的校验大部分同学跟我一一样，都只是知道可以通过`@Valid` / `@Validated`来对接口的入参进行校验，但是对于其底层的具体实现以及一些细节都不是很清楚，通过这篇文章我们就来彻底搞懂Spring中的校验机制。
>
> 在学习Spring中某个功能时，往往要从Java本身出发。比如我们之前介绍过的Spring中的国际化（见《Spring官网阅读（十一）》）、Spring中的`ResolvableType`（见《Spring杂谈》系列文章）等等，它们都是对Java本身的封装，沿着这个思路，我们要学习Spring中的数据校验，必然要先对Java中的数据校验有一定了解。
>
> 话不多说，开始正文！

### Java中的数据校验

> 在学习Java中的数据校验前，我们需要先了解一个概念，即什么是`JSR`?
>
> `JSR`：全称Java Specification Requests，意思是Java 规范提案。我们可以将其理解为Java为一些功能指定的一系列统一的规范。跟数据校验相关的最新的`JSR`为`JSR 380`。
>
> Bean Validation 2.0 是JSR第380号标准。该标准连接如下：https://www.jcp.org/en/egc/view?id=380
> Bean Validation的主页：http://beanvalidation.org
> Bean Validation的参考实现：https://github.com/hibernate/hibernate-validator

#### Bean Validation（JSR 380）

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200406010636463.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)
从官网中的截图我们可以看到，`Bean Validation 2.0`的唯一实现就是[Hibernate Validator](http://hibernate.org/validator/)，对应版本为`6.0.1.Final`，同时在2.0版本之前还有1.1（JSR 349）及1.0（JSR 303）两个版本，不过版本间的差异并不是我们关注的重点，而且`Bean Validation 2.0`本身也向下做了兼容。

> 在上面的图中，可以看到`Bean Validation2.0`的全称为`Jakarta Bean Validation2.0`，关于Jakarta，感兴趣的可以参考这个链接：https://www.oschina.net/news/94055/jakarta-ee-new-logo，就是Java换了个名字。

#### 使用示例

导入依赖：

```xml
<!--除了导入hibernate-validator外，还需要导入一个tomcat-embed-el包，用于提供EL表达式的功能
因为错误message是支持EL表达式计算的，所以需要导入此包
-->
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-el</artifactId>
    <version>9.0.16</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>6.0.14.Final</version>
    <scope>compile</scope>
</dependency>
<!--
如果你用的是一个SpringBoot项目只需要导入下面这个依赖即可

<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

-->
123456789101112131415161718192021222324
```

测试Demo：

```java
@Data
public class Person {
    
    @NotEmpty
    private String name;
    
    @Positive
    @Max(value = 100)
    private int age;
}

public class SpringValidation {
    public static void main(String[] args) {
        Person person = new Person();
        person.setAge(-1);
        Set<ConstraintViolation<Person>> result =
               Validation.buildDefaultValidatorFactory().getValidator().validate(person);
        // 对结果进行遍历输出
        result.stream().map(v -> v.getPropertyPath() + " " + v.getMessage() + ": " + v.getInvalidValue())
                .forEach(System.out::println);
    }
    // 运行结果：
    // name 不能为空: null
    // age 必须是正数: -1
}
12345678910111213141516171819202122232425
```

对于其中涉及的细节目前来说我不打算过多的探讨，我们现在只需要知道Java提供了数据校验的规范，同时Hibernate对其有一套实现就可以了，并且我们也验证了使用其进行校验是可行的。那么接下来我们的问题就变成了Spring对Java的这套数据校验的规范做了什么支持呢？或者它又做了什么扩展呢？

### Spring对Bean Validation的支持

我们先从官网入手，看看Spring中如何使用数据校验，我这里就直接取官网中的Demo了

```java
@Data
public class Person {
    private String name;
    private int age;
}

public class PersonValidator implements Validator {
    @Override
    public boolean supports(Class clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(Object obj, Errors e) {
        ValidationUtils.rejectIfEmpty(e, "name", "name.empty");
        Person p = (Person) obj;
        if (p.getAge() < 0) {
            e.rejectValue("age", "negativevalue");
        } else if (p.getAge() > 110) {
            e.rejectValue("age", "too.darn.old");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Person person = new Person();
        person.setAge(-1);
        DirectFieldBindingResult errorResult = new DirectFieldBindingResult(person, "dmz");
        PersonValidator personValidator = new PersonValidator();
        personValidator.validate(person, errorResult);
        System.out.println(errorResult);
        // 程序打印：
//Field error in object 'dmz' on field 'name': rejected value [null]; codes //[name.empty.dmz.name,name.empty.name,name.empty.java.lang.String,name.empty]; arguments //[]; default message [null]
//Field error in object 'dmz' on field 'age': rejected value [-1]; codes //[negativevalue.dmz.age,negativevalue.age,negativevalue.int,negativevalue]; arguments //[]; default message [null]
        
    }
}
1234567891011121314151617181920212223242526272829303132333435363738
```

在上面的例子中，`PersonValidator`实现了一个`Validator`接口，这个接口是Spring自己提供的，全称：`org.springframework.validation.Validator`，我们看看这个接口的定义

#### Spring中的Validator

> `org.springframework.validation.Validator`是专门用于应用相关的对象的校验器。
>
> 这个接口完全从基础设施或者上下文中脱离的，这意味着它没有跟web层或者数据访问层或者其余任何的某一个层次发生耦合。所以它能用于应用中的任意一个层次，能对应用中的任意一个对象进行校验。，

##### 接口定义

```java
public interface Validator {
	
    // 此clazz是否可以被validate
	boolean supports(Class<?> clazz);
	
    // 执行校验，错误消息放在Errors中
    // 如果能执行校验，通常也意味着supports方法返回true
	// 可以参考ValidationUtils这个工具类
	void validate(Object target, Errors errors);
}
12345678910
```

##### UML类图

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020040601064937.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

###### SmartValidator

> 对Validator接口进行了增强，能进行分组校验

```java
public interface SmartValidator extends Validator {

	// validationHints：就是启动的校验组
    // target：需要校验的结果
    // errors：封装校验
    void validate(Object target, Errors errors, Object... validationHints);
	
    // 假设value将被绑定到指定对象中的指定字段上，并进行校验
    // @since 5.1  这个方法子类需要复写 否则不能使用
    default void validateValue(Class<?> targetType, String fieldName, @Nullable Object value, Errors errors, Object... validationHints) {
        throw new IllegalArgumentException("Cannot validate individual value for " + targetType);
    }
}
12345678910111213
```

###### SpringValidatorAdapter

> 在之前的接口我们会发现，到目前为止Spring中的校验跟Bean Validation还没有产生任何交集，而SpringValidatorAdapter就完成了到Bean Validation的对接

```java
// 可以看到，这个接口同时实现了Spring中的SmartValidator接口跟JSR中的Validator接口
public class SpringValidatorAdapter implements SmartValidator, javax.validation.Validator {
	
    //@NotEmpty,@NotNull等注解都会有这三个属性
	private static final Set<String> internalAnnotationAttributes = new HashSet<>(4);
	static {
		internalAnnotationAttributes.add("message");
		internalAnnotationAttributes.add("groups");
		internalAnnotationAttributes.add("payload");
	}
	
    // targetValidator就是实际完成校验的对象
	@Nullable
	private javax.validation.Validator targetValidator;
	public SpringValidatorAdapter(javax.validation.Validator targetValidator) {
		Assert.notNull(targetValidator, "Target Validator must not be null");
		this.targetValidator = targetValidator;
	}
	SpringValidatorAdapter() {
	}
	void setTargetValidator(javax.validation.Validator targetValidator) {
		this.targetValidator = targetValidator;
	}

    // 支持对所有类型的Bean的校验
	@Override
	public boolean supports(Class<?> clazz) {
		return (this.targetValidator != null);
	}
	
    // 调用targetValidator完成校验，并通过processConstraintViolations方法封装校验后的结果到Errors中
	@Override
	public void validate(Object target, Errors errors) {
		if (this.targetValidator != null) {
			processConstraintViolations(this.targetValidator.validate(target), errors);
		}
	}
	
    // 完成分组校验
	@Override
	public void validate(Object target, Errors errors, Object... validationHints) {
		if (this.targetValidator != null) {
			processConstraintViolations(
					this.targetValidator.validate(target, asValidationGroups(validationHints)), errors);
		}
	}
	
    // 完成对对象上某一个字段及给定值的校验
	@SuppressWarnings("unchecked")
	@Override
	public void validateValue(
			Class<?> targetType, String fieldName, @Nullable Object value, Errors errors, Object... validationHints) {

		if (this.targetValidator != null) {
			processConstraintViolations(this.targetValidator.validateValue(
					(Class) targetType, fieldName, value, asValidationGroups(validationHints)), errors);
		}
	}


	// @since 5.1
	// 将validationHints转换成JSR中的分组
	private Class<?>[] asValidationGroups(Object... validationHints) {
		Set<Class<?>> groups = new LinkedHashSet<>(4);
		for (Object hint : validationHints) {
			if (hint instanceof Class) {
				groups.add((Class<?>) hint);
			}
		}
		return ClassUtils.toClassArray(groups);
	}

	// 省略对校验错误的封装
    // .....


	
    // 省略对JSR中validator接口的实现，都是委托给targetValidator完成的
    // ......

}

12345678910111213141516171819202122232425262728293031323334353637383940414243444546474849505152535455565758596061626364656667686970717273747576777879808182
```

###### ValidatorAdapter

> 跟SpringValidatorAdapter同一级别的类，但是不同的是他没有实现JSR中的Validator接口。一般不会使用这个类

###### CustomValidatorBean

```java
public class CustomValidatorBean extends SpringValidatorAdapter implements Validator, InitializingBean {
	
    // JSR中的接口，校验器工厂
	@Nullable
	private ValidatorFactory validatorFactory;
	
    // JSR中的接口，用于封装校验信息
	@Nullable
	private MessageInterpolator messageInterpolator;
	
     // JSR中的接口，用于判断属性能否被ValidatorProvider访问
	@Nullable
	private TraversableResolver traversableResolver;

	// 忽略setter方法
	
    // 在SpringValidatorAdapter的基础上实现了InitializingBean，在Bean初始化时调用，用于给上面三个属性进行配置
	@Override
	public void afterPropertiesSet() {
		if (this.validatorFactory == null) {
			this.validatorFactory = Validation.buildDefaultValidatorFactory();
		}

		ValidatorContext validatorContext = this.validatorFactory.usingContext();
		MessageInterpolator targetInterpolator = this.messageInterpolator;
		if (targetInterpolator == null) {
			targetInterpolator = this.validatorFactory.getMessageInterpolator();
		}
		validatorContext.messageInterpolator(new LocaleContextMessageInterpolator(targetInterpolator));
		if (this.traversableResolver != null) {
			validatorContext.traversableResolver(this.traversableResolver);
		}

		setTargetValidator(validatorContext.getValidator());
	}

}
12345678910111213141516171819202122232425262728293031323334353637
```

###### LocalValidatorFactoryBean

```java
public class LocalValidatorFactoryBean extends SpringValidatorAdapter
    implements ValidatorFactory, ApplicationContextAware, InitializingBean, DisposableBean {
		//......
}
1234
```

可以看到，这个类额外实现了`ValidatorFactory`接口，所以通过它不仅能完成校验，还能获取一个校验器validator。

###### OptionalValidatorFactoryBean

```java
public class OptionalValidatorFactoryBean extends LocalValidatorFactoryBean {

	@Override
	public void afterPropertiesSet() {
		try {
			super.afterPropertiesSet();
		}
		catch (ValidationException ex) {
			LogFactory.getLog(getClass()).debug("Failed to set up a Bean Validation provider", ex);
		}
	}

}
12345678910111213
```

继承了LocalValidatorFactoryBean，区别在于让校验器的初始化成为可选的，即使校验器没有初始化成功也不会报错。

#### 使用示例

在对整个体系有一定了解之后，我们通过一个例子来体会下Spring中数据校验

```java
public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
        // 将CustomValidatorBean注册到容器中，主要是为了让它经过初始化阶段完成对校验器的配置
        ac.register(CustomValidatorBean.class);
        // 刷新启动容器
        ac.refresh();
        // 获取到容器中的校验器
        CustomValidatorBean cb = ac.getBean(CustomValidatorBean.class);

        // 校验simple组的校验
        Person person = new Person();
        DirectFieldBindingResult simpleDbr = new DirectFieldBindingResult(person, "person");
        cb.validate(person, simpleDbr, Person.Simple.class);

        // 校验Complex组的校验
        DirectFieldBindingResult complexDbr = new DirectFieldBindingResult(person, "person");
        person.setStart(new Date());
        cb.validate(person, complexDbr, Person.Complex.class);
        System.out.println(complexDbr);
    }
}
12345678910111213141516171819202122
```

运行结果我这里就不贴出来了，大家可以自行测试

------

到目前为止，我们所接触到的校验的内容跟实际使用还是有很大区别，我相信在绝大多数情况下大家都不会采用前文所采用的这种方式去完成校验，而是通过`@Validated`或者`@Valid`来完成校验。

### @Validated跟@Valid的区别

关于二者的区别网上有很多文章，但是实际二者的区别大家不用去记，我们只要看一看两个注解的申明变一目了然了。

#### @Validated

```java
// Target代表这个注解能使用在类/接口/枚举上，方法上以及方法的参数上
// 注意注意！！！！ 它不能注解到字段上
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
// 在运行时期仍然生效（注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在）
@Retention(RetentionPolicy.RUNTIME)
// 这个注解应该被 javadoc工具记录. 默认情况下,javadoc是不包括注解的. 但如果声明注解时指定了 @Documented,则它会被 javadoc 之类的工具处理, 所以注解类型信息也会被包括在生成的文档中，是一个标记注解，没有成员。
@Documented
public @interface Validated {
	// 校验时启动的分组
	Class<?>[] value() default {};

}
123456789101112
```

#### @Valid

```java
// 可以作用于类，方法，字段，构造函数，参数，以及泛型类型上(例如：Main<@Valid T> )
// 简单来说，哪里都可以放
@Target({ METHOD, FIELD, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
public @interface Valid {
    //没有提供任何属性
}
12345678
```

我们通过上面两个注解的定义就能很快的得出它们的区别：

1. **来源不同**，`@Valid`是`JSR`的规范，来源于`javax.validation`包下，而`@Validated`是Spring自身定义的注解，位于`org.springframework.validation.annotation`包下

2. **作用范围不同**，`@Validated`无法作用在字段上，正因为如此它就无法完成对级联属性的校验。而`@Valid`的

   没有这个限制。

3. **注解中的属性不同**，`@Validated`注解中可以提供一个属性去指定校验时采用的分组，而`@Valid`没有这个功能，因为`@Valid`不能进行分组校验

> 我相信通过这个方法的记忆远比看博客死记要好~

### 实际生产应用

> 我们将分为两部分讨论
>
> 1. 对Java的校验
> 2. 对普通参数的校验
>
> 这里说的普通参数的校验是指参数没有被封装到JavaBean中，而是直接使用，例如：
>
> test(String name,int age)，这里的name跟age就是简单的参数。
>
> 而将name跟age封装到JavaBean中，则意味着这是对JavaBean的校验。
>
> 同时，按照校验的层次，我们可以将其分为
>
> 1. 对controller层次（接口层）的校验
> 2. 对普通方法的校验
>
> 接下来，我们就按这种思路一一进行分析
>
> 子所以按照层次划分是因为Spring在对接口上的参数进行校验时，跟对普通的方法上的参数进行校验采用的是不同的形式（*虽然都是依赖于JSR的实现来完成的，但是调用JSR的手段不一样*）

#### 对JavaBean的校验

**待校验的类**

```java
@Data
public class Person {

    // 错误消息message是可以自定义的
    @NotNull//(groups = Simple.class)
    public String name;

    @Positive//(groups = Default.class)
    public Integer age;

    @NotNull//(groups = Complex.class)
    @NotEmpty//(groups = Complex.class)
    private List<@Email String> emails;

    // 定义两个组 Simple组和Complex组
    public interface Simple {
    }

    public interface Complex {

    }
}

// 用于进行嵌套校验
@Data
public class NestPerson {
    @NotNull
    String name;

    @Valid
    Person person;
}
1234567891011121314151617181920212223242526272829303132
```

###### 对controller（接口）层次上方法参数的校验

**用于测试的接口**

```java
// 用于测试的接口
@RestController
@RequestMapping("/test")
public class Main {
	
    // 测试 @Valid对JavaBean的校验效果
    @RequestMapping("/valid")
    public String testValid(
            @Valid @RequestBody Person person) {
        System.out.println(person);
        return "OK";
    }

    // 测试 @Validated对JavaBean的校验效果
    @RequestMapping("/validated")
    public String testValidated(
            @Validated @RequestBody Person person) {
        System.out.println(person);
        return "OK";
    }
    
    // 测试 @Valid对JavaBean嵌套属性的校验效果
    @RequestMapping("/validNest")
    public String testValid(@Valid @RequestBody NestPerson person) {
        System.out.println(person);
        return "OK";
    }
	
    // 测试 @Validated对JavaBean嵌套属性的校验效果
    @RequestMapping("/validatedNest")
    public String testValidated(@Validated @RequestBody NestPerson person) {
        System.out.println(person);
        return "OK";
    }
}
1234567891011121314151617181920212223242526272829303132333435
```

**测试用例**

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringFxApplication.class)
public class MainTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    ObjectMapper objectMapper;

    MockMvc mockMvc;

    Person person;

    NestPerson nestPerson;

    @Before
    public void init() {
        person = new Person();
        person.setAge(-1);
        person.setName("");
        person.setEmails(new ArrayList<>());
        nestPerson = new NestPerson();
        nestPerson.setPerson(person);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testValid() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/test/valid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(person));
        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
        Exception resolvedException = mvcResult.getResolvedException();
        System.out.println(resolvedException.getMessage());
        assert mvcResult.getResponse().getStatus()==200;
    }

    @Test
    public void testValidated() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/test/validated")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(person));
        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
        Exception resolvedException = mvcResult.getResolvedException();
        System.out.println(resolvedException.getMessage());
        assert mvcResult.getResponse().getStatus()==200;
    }

    @Test
    public void testValidNest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/test/validatedNest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nestPerson));
        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
        Exception resolvedException = mvcResult.getResolvedException();
        System.out.println(resolvedException.getMessage());
        assert mvcResult.getResponse().getStatus()==200;
    }

    @Test
    public void testValidatedNest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/test/validatedNest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nestPerson));
        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
        Exception resolvedException = mvcResult.getResolvedException();
        System.out.println(resolvedException.getMessage());
        assert mvcResult.getResponse().getStatus()==200;
    }

}
123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172
```

**测试结果**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200406010707139.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

我们执行用例时会发现，四个用例均断言失败并且控制台打印：Validation failed for argument …。

另外细心的同学可以发现，Spring默认有一个全局异常处理器`DefaultHandlerExceptionResolver`

同时观察日志我们可以发现，全局异常处理器处理的异常类型为：`org.springframework.web.bind.MethodArgumentNotValidException`

**使用注意要点**

> 1. **如果想使用分组校验的功能必须使用@Validated**
> 2. 不考虑分组校验的情况，`@Validated`跟`@Valid`没有任何区别
> 3. 网上很多文章说`@Validated`不支持对嵌套的属性进行校验，这种说法是不准确的，大家可以对第三，四个接口方法做测试，运行的结果是一样的。更准确的说法是`@Validated`不能作用于字段上，而`@Valid`可以。

###### 对普通方法的校验

待测试的方法

```java
@Service
//@Validated
//@Valid
public class DmzService {
    public void testValid(@Valid Person person) {
        System.out.println(person);
    }

    public void testValidated(@Validated Person person) {
        System.out.println(person);
    }
}
123456789101112
```

测试用例

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringFxApplication.class)
public class DmzServiceTest {

    @Autowired
    DmzService dmzService;

    Person person;

    @Before
    public void init(){
        person = new Person();
        person.setAge(-1);
        person.setName("");
        person.setEmails(new ArrayList<>());
    }

    @Test
    public void testValid() {
        dmzService.testValid(person);
    }

    @Test
    public void testValidated() {
        dmzService.testValidated(person);
    }
}
123456789101112131415161718192021222324252627
```

我们分为三种情况测试

1. **类上不添加任何注解**

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020040601071929.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

1. **类上添加@Validated注解**

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020040601072831.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

1. **类上添加@Valid注解**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200406010736710.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

**使用注意要点**

> 通过上面的例子，我们可以发现，只有类上添加了`@Vlidated`注解，并且待校验的JavaBean上添加了`@Valid`的情况下校验才会生效。
>
> 所以当我们要对普通方法上的JavaBean参数进行校验必须满足下面两个条件
>
> 1. 方法所在的类上添加`@Vlidated`
> 2. 待校验的JavaBean参数上添加`@Valid`

------

#### 对简单参数校验

###### 对普通方法的校验

**用于测试的方法**

```java
@Service
@Validated
//@Valid
public class IndexService {
    public void testValid(@Max(10) int age,@NotBlank String name) {
        System.out.println(age+"     "+name);
    }

    public void testValidated(@Max(10) int age,@NotBlank String name) {
        System.out.println(age+"     "+name);
    }

    public void testValidNest(@Max(10) int age,@NotBlank String name) {
        System.out.println(age+"     "+name);
    }

    public void testValidatedNest(@Max(10) int age,@NotBlank String name) {
        System.out.println(age+"     "+name);
    }
}
1234567891011121314151617181920
```

**测试用例**

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringFxApplication.class)
public class IndexServiceTest {
    @Autowired
    IndexService indexService;

    int age;

    String name;

    @Before
    public void init(){
        age=100;
        name = "";
    }
    @Test
    public void testValid() {
        indexService.testValid(age,name);
    }
    @Test
    public void testValidated() {
        indexService.testValidated(age,name);
    }
    @Test
    public void testValidNest() {
        indexService.testValidNest(age,name);
    }
    @Test
    public void testValidatedNest() {
        indexService.testValidatedNest(age,name);
    }
}
1234567891011121314151617181920212223242526272829303132
```

这里的测试结果我就不再放出来了，大家猜也能猜到答案

**使用注意要点**

> 1. 方法所在的类上添加`@Vlidated`（`@Valid`注解无效），跟JavaBean的校验是一样的

###### 对controller（接口）层次的校验

```java
@RestController
@RequestMapping("/test/simple")
// @Validated
public class ValidationController {

    @RequestMapping("/valid")
    public String testValid(
            @Valid @Max(10) int age, @Valid @NotBlank String name) {
        System.out.println(age + "      " + name);
        return "OK";
    }

    @RequestMapping("/validated")
    public String testValidated(
            @Validated @Max(10) int age, @Valid @NotBlank String name) {
        System.out.println(age + "      " + name);
        return "OK";
    }
}
12345678910111213141516171819
```

在测试过程中会发现，不过是在参数前添加了`@Valid`或者`@Validated`校验均不生效。这个时候不得不借助Spring提供的普通方法的校验功能来完成数据校验，也就是在类级别上添加`@Valiv=dated`（参数前面的`@Valid`或者`@Validated`可以去除）

使用注意要点

> 对于接口层次简单参数的校验需要借助Spring对于普通方法校验的功能，必须在类级别上添加`@Valiv=dated`注解。

**注意**

在上面的所有例子中我都是用SpringBoot进行测试的，如果在单纯的SpringMVC情况下，如果对于普通方法的校验不生效请添加如下配置：

```java
@Bean
public MethodValidationPostProcessor methodValidationPostProcessor() {
    return new MethodValidationPostProcessor();
}
1234
```

实际上对于普通方法的校验，就是通过这个后置处理器来完成的，它会生成一个代理对象帮助我们完成校验。SpringBoot中默认加载了这个后置处理器，而SpringMVC需要手动配置

#### 结合BindingResult使用

在上面的例子中我们可以看到，当对于接口层次的JavaBean进行校验时，如果校验失败将会抛出`org.springframework.web.bind.MethodArgumentNotValidException`异常，这个异常将由Spring默认的全局异常处理器进行处理，但是有时候我们可能想在接口中拿到具体的错误进行处理，这个时候就需要用到`BindingResult`了

如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200406010746885.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

可以发现，错误信息已经被封装到了`BindingResult`，通过`BindingResult`我们能对错误信息进行自己的处理。请注意，这种做法只对接口中JavaBean的校验生效，对于普通参数的校验是无效的。

------

> 实际上经过上面的学习我们会发现，其实Spring中的校验就是两种（前面的分类是按场景分的）
>
> 1. Spring在接口上对JavaBean的校验
> 2. Spring在普通方法上的校验
>
> 第一种校验失败将抛出`org.springframework.web.bind.MethodArgumentNotValidException`异常，而第二种校验失败将抛出`javax.validation.ConstraintViolationException`异常
>
> 为什么会这样呢？
>
> 这是因为，对于接口上JavaBean的校验是Spring在对参数进行绑定时做了一层封装，大家可以看看`org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor#resolveArgument`这段代码
>
> ```java
> 	public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
> 			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
> 
> 		parameter = parameter.nestedIfOptional();
> 		Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
> 		String name = Conventions.getVariableNameForParameter(parameter);
> 
> 		if (binderFactory != null) {
>             // 获取一个DataBinder
> 			WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);
> 			if (arg != null) {
>                 // 进行校验，实际上就是调用DataBinder完成校验
> 				validateIfApplicable(binder, parameter);
>                 // 如果校验出错并且没有提供BindingResult直接抛出一个MethodArgumentNotValidException
> 				if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
> 					throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
> 				}
> 			}
> 			if (mavContainer != null) {
> 				mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
> 			}
> 		}
> 
> 		return adaptArgumentIfNecessary(arg, parameter);
> 	}
> 12345678910111213141516171819202122232425
> ```
>
> 但是对于普通方法的校验时，Spring完全依赖于动态代理来完成参数的校验。具体细节在本文中不多赘述，大家可以关注我后续文章，有兴趣的同学可以看看这个后置处理器：`MethodValidationPostProcessor`

#### 结合全局异常处理器使用

在实际应用中，更多情况下我们结合全局异常处理器来使用数据校验的功能，实现起来也非常简单，如下：

```java
@RestControllerAdvice
public class MethodArgumentNotValidExceptionHandler {
	// 另外还有一个javax.validation.ConstraintViolationException异常处理方式也类似，这里不再赘述
    // 关于全局异常处理器的部分因为是跟SpringMVC相关的，另外牵涉到动态代理，所以目前我也不想做过多介绍
    // 大家只要知道能这么用即可，实际的使用可自行百度，非常简单
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();

        StringBuilder stringBuilder = new StringBuilder();
        for (FieldError error : bindingResult.getFieldErrors()) {
            String field = error.getField();
            Object value = error.getRejectedValue();
            String msg = error.getDefaultMessage();
            String message = String.format("错误字段：%s，错误值：%s，原因：%s；", field, value, msg);
            stringBuilder.append(message).append("\r\n");
        }
        return Result.error(MsgDefinition.ILLEGAL_ARGUMENTS.codeOf(), stringBuilder.toString());
    }
}
1234567891011121314151617181920
```

### 总结

关于数据校验我们就介绍到这里了，其实我自己之前对Spring中具体的数据校验的使用方法及其原理都非常的模糊，但是经过这一篇文章的学习，现在可以说知道自己用了什么了并且知道怎么用，也知道为什么。这也是我写这篇文章的目的。按照惯例，我们还是总结了一张图，如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200406010756462.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70)

## Spring官网阅读（十八）AOP的核心概

> 本篇文章将作为整个Spring官网阅读笔记的最后一篇。如果要谈`SpringFramework`必定离不开两点
>
> 1. `IOC`（控制反转）
> 2. `AOP`（面向切面）
>
> 在前面的文章中我们已经对`IOC`做过详细的介绍了，本文主要介绍`AOP`，关于其中的源码部分将在专门的源码专题介绍，本文主要涉及的是`AOP`的基本概念以及如何使用,本文主要涉及到官网中的第5、6两大章

### 什么是AOP

> `AOP`为Aspect Oriented Programming的缩写，意为：[面向切面编程](https://baike.baidu.com/item/面向切面编程/6016335)，通过[预编译](https://baike.baidu.com/item/预编译/3191547)方式和运行期间动态代理实现程序功能的统一维护的一种技术。`AOP`是[OOP](https://baike.baidu.com/item/OOP)的延续，是软件开发中的一个热点，也是[Spring](https://baike.baidu.com/item/Spring)框架中的一个重要内容，是[函数式编程](https://baike.baidu.com/item/函数式编程/4035031)的一种衍生范型。利用`AOP`可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的[耦合度](https://baike.baidu.com/item/耦合度/2603938)降低，提高程序的可重用性，同时提高了开发的效率。
>
>  ------------------《百度百科》

可能你看完上面这段话还是迷迷糊糊，一堆专业词汇看起来很牛逼的样子，不用担心，任何东西都是需要积累的，有些东西只需要记在脑子里，在以后实践的过程中自然而然的就明白了。

另外放一段网上大佬写的一段话，我觉得很好的解释了面向对象跟面向切面

> 面向对象编程**解决了业务模块的封装复用**的问题，但是对于某些模块，其本身并不独属于摸个业务模块，而是根据不同的情况，贯穿于某几个或全部的模块之间的。例如登录验证，其只开放几个可以不用登录的接口给用户使用（一般登录使用拦截器实现，但是其切面思想是一致的）；再比如性能统计，其需要记录每个业务模块的调用，并且监控器调用时间。可以看到，**这些横贯于每个业务模块的模块，如果使用面向对象的方式，那么就需要在已封装的每个模块中添加相应的重复代码**，对于这种情况，面向切面编程就可以派上用场了。
>
> 面向切面编程，指的是将一定的切面逻辑按照一定的方式编织到指定的业务模块中，从而将这些业务模块的调用包裹起来

### AOP中的核心概念

官网中的相关介绍如下：

![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128123856820.png)

是不是看得头皮发麻，不用紧张，我们一点点看过去就好了，现在从上往下开始介绍

> 前置场景，假设我们现在要对所有的controller层的接口进行性能监控

#### 切面Aspect

> 切点跟通知组成了切面

#### 连接点Joint Point

> 所有我们能够将通知应用到的地方都是连接点，在Spring中，我们可以认为连接点就是所有的方法（除构造函数外），连接点没有什么实际意义，这个概念的提出只是为了更好的说明切点

#### 通知Advice

> 就是我们想要额外实现的功能，在上面的例子中，实现了性能监控的方法就是通知

#### 切点Pointcut

> 在连接点的基础上，来定义切入点，比如在我们上面的场景中，要对controller层的所有接口完成性能监控，那么就是说所有controller中的方法就是我们的切点（service层，dao层的就是普通的连接点，没有什么作用）。

#### 引入Introduction

> 我们可以让代理类实现目标类没有实现的额外的接口以及持有新的字段。

#### 目标对象Target object

> 引入中所提到的目标类，也就是要被通知的对象，也就是真正的业务逻辑，他可以在毫不知情的情况下，被织入切面，而自己专注于业务本身的逻辑。

#### 代理对象AOP proxy

> 将切面织入目标对象后所得到的就是代理对象。代理对象是正在具备通知所定义的功能，并且被引入了的对象。

#### 织入Weaving

> 把切面应用到目标对象来创建新的代理对象的过程。切面的织入有三种方式
>
> 1. 编译时织入
> 2. 类加载时期织入
> 3. 运行时织入
>
> 我们通常使用的`SpringAOP`都是运行时期织入，另外Spring中也提供了一个Load Time Weaving（`LTW`，加载时期织入）的功能，此功能使用较少，有兴趣的同学可以参考一下两个链接：
>
> - https://www.cnblogs.com/wade-luffy/p/6073702.html
> - https://docs.spring.io/spring/docs/5.1.14.BUILD-SNAPSHOT/spring-framework-reference/core.html#aop-aj-ltw

------

> *上面这些名词希望你不仅能记住而且要能理解，因为不管是Spring源码还是官网中都使用了这些名词，并且从这些名称中还衍生了一些新的名词，比如：`Advisor`，虽然这些在源码阶段会再介绍，不过如果现在能懂的话无疑就在为学习源码减负了。*

在对`AOP`中的核心概念有了一定了解之后，我们就来看看，如何使用`AOP`，在学习使用时，第一步我们需要知道怎么去在容器中申明上面所说的那些`AOP`中的元素

### Spring中如何使用AOP

> XML方式本文不再介绍了，笔者近两年来没有通过XML的方式来使用过SpringAOP，现在注解才是王道，本文也只会介绍注解的方式

#### 1、开启AOP

```java
@Configuration
@ComponentScan("com.spring.study.springfx.aop")
@EnableAspectJAutoProxy  // 开启AOP
public class Config {
}
```

核心点就是在配置类上添加`@EnableAspectJAutoProxy`,这个注解中有两个属性如下：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {
	// 是否使用CGLIB代理，默认不使用。默认使用JDK动态代理
	boolean proxyTargetClass() default false;
	
  // 是否将代理类作为线程本地变量（threadLocal）暴露（可以通过AopContext访问）
  // 主要设计的目的是用来解决内部调用的问题
	boolean exposeProxy() default false;

}
```

#### 2、申明切面

```java
@Aspect     // 申明是一个切面
@Component  // 切记，一定要将切面交由Spring管理，否则不起作用
public class DmzAnnotationAspect {
	//......
}
```

#### 3、申明切点

> 我们一般都会通过<u>***切点表达式***</u>来申明切点，切点表达式一般可以分为以下几种

切点表达式

##### excecution表达式

语法

> ```
> execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern) throws-pattern?)
> ```

这里问号表示当前项是非必填的，其中各项的语义如下：

- modifiers-pattern（**非必填**）：方法的可见性，如public，protected；
- ret-type-pattern（**必填**）：方法的返回值类型，如int，void等；
- declaring-type-pattern（**非必填**）：方法所在类的全路径名，如com.spring.Aspect；
- name-pattern（**必填**）：方法名类型，如buisinessService()；
- param-pattern（**必填**）：方法的参数类型，如java.lang.String；
- throws-pattern（**非必填**）：方法抛出的异常类型，如java.lang.Exception；

可以看到，必填的参数只有三个，**返回值**，**方法名**，**方法参数**。

示例

按照上面的语法，我们可以定义如下的切点表达式

> ```java
> // 1.所有权限为public的，返回值不限，方法名称不限，方法参数个数及类型不限的方法，简而言之，所有public的方法
> execution(public * *(..))
> 
> // 2.所有权限为public的，返回值限定为String的，方法名称不限，方法参数个数及类型不限的方法
> execution(public java.lang.String *(..)) 
> 
> // 3.所有权限为public的，返回值限定为String的，方法名称限定为test开头的，方法参数个数及类型不限的方法
> execution(public java.lang.String test*(..))
>  
> // 4.所有权限为public的，返回值限定为String的，方法所在类限定为com.spring.study.springfx.aop.service包下的任意类，方法名称限定为test开头的，方法参数个数及类型不限的方法
> execution(public java.lang.String com.spring.study.springfx.aop.service.*.test*(..))
>      
> // 5.所有权限为public的，返回值限定为String的，方法所在类限定为com.spring.study.springfx.aop.service包及其子包下的任意类，方法名称限定为test开头的，方法参数个数及类型不限的方法
> execution(public java.lang.String com.spring.study.springfx.aop.service..*.test*(..))
>  
> // 6.所有权限为public的，返回值限定为String的，方法所在类限定为com.spring.study.springfx.aop.service包及其子包下的Dmz开头的类，方法名称限定为test开头的，方法参数个数及类型不限的方法
> execution(public java.lang.String com.spring.study.springfx.aop.service..Dmz*.test*(..))
>  
> // 7.所有权限为public的，返回值限定为String的，方法所在类限定为com.spring.study.springfx.aop.service包及其子包下的Dmz开头的类，方法名称限定为test开头的，方法参数限定第一个为String类，第二个不限但是必须有两个参数
> execution(public java.lang.String com.spring.study.springfx.aop.service..Dmz*.test*(String,*))
> 
> // 8.所有权限为public的，返回值限定为String的，方法所在类限定为com.spring.study.springfx.aop.service包及其子包下的Dmz开头的类，方法名称限定为test开头的，方法参数限定第一个为String类，第二个可有可无并且不限定类型
> execution(public java.lang.String com.spring.study.springfx.aop.service..Dmz*.test*(String,..))
> ```

看完上面的例子不知道大家有没有疑问，比如为什么修饰符一直是`public`呢？其它修饰符行不行呢？修饰符的位置能不能写成`*`这种形式呢？

> 答：
>
> 1. 如果使用的是JDK动态代理，这个修饰符必须是public，因为JDK动态代理是针对于目标类实现的接口进行的，接口的实现方法必定是public的。
> 2. 如果不使用JDK动态代理而使用CGLIB代理（`@EnableAspectJAutoProxy(proxyTargetClass = true)`）那么修饰符还可以使用protected或者默认修饰符。但是不能使用private修饰符，因为CGLIB代理生成的代理类是继承目标类的，private方法子类无法复写，自然也无法代理。基于此，修饰符是不能写成`*`这种格式的。

##### @annotation表达式

语法

> @annotation(annotation-type)

示例

> ```java
> // 代表所有被DmzAnnotation注解所标注的方法
> // 使用注解的方法定义切点一般会和自定义注解配合使用
> @annotation(com.spring.study.springfx.aop.annotation.DmzAnnotation)
> ```

##### within表达式

语法

> within(declaring-type-pattern)

示例

> ```java
> // within表达式只能指定到类级别，如下示例表示匹配com.spring.service.BusinessObject中的所有方法
> within(com.spring.service.BusinessObject)
>  
> // within表达式能够使用通配符，如下表达式表示匹配com.spring.service包（不包括子包）下的所有类
> within(com.spring.service.*)        
> 
> // within表达式能够使用通配符，如下表达式表示匹配com.spring.service包及子包下的所有类
> within(com.spring.service..*)    
> ```

*官网中一共给出了9中切点表达式的定义方式，但是实际上我们常用的就两种，就是`excecution表达式`以及`annotation表达式`。所以下文对于其余几种本文就不做详细的介绍了，大家有兴趣的可以了解，没有兴趣的可以直接跳过。可以参考[官网链接](https://docs.spring.io/spring/docs/5.1.14.BUILD-SNAPSHOT/spring-framework-reference/core.html#aop-pointcuts-designators)*

##### @within表达式

语法

> @within(annotation-type)

跟`annotation表达式`的区别在于，`annotation表达式`是面向方法的，表示匹配带有指定注解的方法，而`within表达式`是面向类的，表示匹配带有指定注解的类。

示例

> ```java
> // 代表所有被DmzAnnotation注解所标注的类
> // 使用注解的方法定义切点一般会和自定义注解配合使用
> @within(com.spring.study.springfx.aop.annotation.DmzAnnotation)
> ```

##### arg表达式

语法

> args(param-pattern)

示例

> ```java
> // 匹配所有只有一个String类型的方法
> args(String)
> // 匹配所有有两个参数并且第一个参数为String的方法
> args(String,*)
> // 匹配所有第一个参数是String类型参数的方法
> args(String,..)
> ```

##### @args表达式

语法

> @args(annotation-type)

示例

> @args(com.spring.annotation.FruitAspect)

跟`@annotation表达式`以及`@within表达式`类似，`@annotation表达式`表示匹配使用了指定注解的方法，`@within表达式`表达式表示匹配了使用了指定注解的类，而`@args表达式`则代表使用了被指定注解标注的类作为方法参数

##### this表达式

> ```java
> // 代表匹配所有代理类是AccountService的类
> this(com.xyz.service.AccountService)
> ```

##### target表达式

> ```java
> // 代表匹配所有目标类是AccountService的类
> target(com.xyz.service.AccountService)
> ```

*this跟target很鸡肋，基本用不到*

##### 定义切点

```java
@Aspect
@Component
public class DmzAnnotationAspect {
    @Pointcut("execution(public * *(..))")
    private void executionPointcut() {}
	
    @Pointcut("@annotation(com.spring.study.springfx.aop.annotation.DmzAnnotation)")
    private void annotationPointcut() { }
    
    // 可以组合使用定义好的切点
    
    // 表示同时匹配满足两者
    @Pointcut("executionPointcut() && annotationPointcut()")
    private void annotationPointcutAnd() {}
	
    // 满足其中之一即可
    @Pointcut("executionPointcut() || annotationPointcut()")
    private void annotationPointcutOr() {}
	
    // 不匹配即可
    @Pointcut("!executionPointcut()")
    private void annotationPointcutNot() {}
}
```

现在我们已经在一个切面中定义了两个切点了，现在开始编写通知

#### 4、申明通知

##### 通知的类型

###### Before

> 在目标方法之前执行，如果发生异常，会阻止业务代码的执行

###### AfterReturning

> 跟Before对应，在目标方法完全执行后（return后）再执行

###### **AfterThrowing**

> 方法抛出异常这个通知仍然会执行（这里的方法既可以是目标方法，也可以是我们定义的通知）

###### After（Finally）

> 切记，跟Before对应的是AfterReturning，一个在目标方法还没执行前执行，一个在目标方法完全执行后（return后）再执行，这个After类型的通知类型我们在编写代码时的Finally，即使方法抛出异常这个通知仍然会执行（这里的方法既可以是目标方法，也可以是我们定义的通知）。
>
> 一般我们使用After类型的通知都是为了完成资源的释放或者其它类似的目的

###### Around

> 最强大的通知类型，可以包裹目标方法，其可以传入一个ProceedingJoinPoint用于调用业务模块的代码，无论是调用前逻辑还是调用后逻辑，都可以在该方法中编写，甚至其可以根据一定的条件而阻断业务模块的调用，可以更改目标方法的返回值

##### 实际应用

```java
@Aspect
@Component
public class DmzAspect {
	
    // 申明的切点
    @Pointcut("execution(public * *(..))")
    private void executionPointcut() {}
    @Pointcut("@annotation(com.spring.study.springfx.aop.annotation.DmzAnnotation)")
    private void annotationPointcut() {}
	
    // 前置通知，在目标方法前调用
    @Before("executionPointcut()")
    public void executionBefore() {
        System.out.println("execution aspect Before invoke!");
    }
    
    // 后置通知，在目标方法返回后调用
    @AfterReturning("executionPointcut()")
    public void executionAfterReturning() {
        System.out.println("execution aspect AfterReturning invoke!");
    }
	
    // 最终通知，正常的执行时机在AfterReturning之前
    @After("executionPointcut()")
    public void executionAfter() {
        System.out.println("execution aspect After invoke!");
    }

 	// 异常通知，发生异常时调用
    @AfterThrowing("executionPointcut()")
    public void executionAfterThrowing() {
        System.out.println("execution aspect AfterThrowing invoke!");
    }
	
    // 环绕通知，方法调用前后都能进行处理
    @Around("executionPointcut()")
    public void executionAround(ProceedingJoinPoint pjp) throws Throwable{
        System.out.println("execution aspect Around(before) invoke!");
        System.out.println(pjp.proceed());
        System.out.println("execution aspect Around(after) invoke!");
    }
}
```

##### 通知中的参数

> 在上面应用的例子中，只有在**环绕通知**的方法上我添加了一个`ProceedingJoinPoint`类型的参数。这个`ProceedingJoinPoint`意味着当前执行中的方法，它继承了`JoinPoint`接口。

###### JoinPoint

JoinPoint可以在任意的通知方法上作为**第一个参数**申明，代表的时候通知所应用的切点（也就是目标类中的方法），它提供了以下几个方法：

- `getArgs()`: 返回当前的切点的参数
- `getThis()`: 返回代理对象
- `getTarget()`: 返回目标对象
- `getSignature()`: 返回这个目标类中方法的描述信息，比如修饰符，名称等

###### ProceedingJoinPoint

ProceedingJoinPoint在JoinPoint的基础上多提供了两个方法

- proceed()：直接执行当前的方法，基于此，我们可以在方法的执行前后直接加入对应的业务逻辑
- proceed(Object[] args)：可以改变当前执行方法的参数，然后用改变后的参数执行这个方法

##### 通知的排序

当我们对于一个切点定义了多个通知时，例如，在一个切点上同时定义了两个before类型的通知。这个时候，为了让这两个通知按照我们期待的顺序执行，我们需要在切面上添加`org.springframework.core.annotation.Order`注解或者让切面实现`org.springframework.core.Ordered`接口。如下：

```java
@Aspect
@Component
@Order(-1)
public class DmzFirstAspect {
    // ...
}

@Aspect
@Component
@Order(0)
public class DmzSecondAspect {
    // ...
}
12345678910111213
```

### AOP的应用

AOP的实际应用非常多，我这里就给出两个例子

1. 全局异常处理器
2. 利用AOP打印接口日志

#### 全局异常处理器

> 需要用到两个注解：`@RestControllerAdvice`及`@ExceptionHandler`总共分为以下几步：
>
> 1. 定义自己项目中用到的错误码及对应异常信息
> 2. 封装自己的异常
> 3. 申明全局异常处理器并针对业务中的异常做统一处理

##### 定义错误码及对应异常信息

```java
@AllArgsConstructor
@Getter
public enum ErrorCode {

    INTERNAL_SERVICE_ERROR(500100, "服务端异常"),

    PASSWORD_CAN_NOT_BE_NULL(500211, "登录密码不能为空"),

    PASSWORD_ERROR(500215, "密码错误");
    
    private int code;

    private String msg;
}

// 统一返回的参数
@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;
    
    
    public static <T> Result<T> success(T data){
        return new Result<T>(data);
    }
    
    public static <T> Result<T> error(ErrorCode cm){
        return new Result<T>(cm.getMsg);
    }
}
```

##### 封装对应异常

```java
public class GlobalException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private int errorCode;
    
    public CreativeArtsShowException(int errorCode) {
        this.errorCode = errorCode;
    }

    public CreativeArtsShowException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode =  errorCode.getCode();
    }
}
```

##### 申明异常处理器

```java
//该注解定义全局异常处理类
//@ControllerAdvice
//@ResponseBody
//使用@RestControllerAdvice可以替代上面两个注解
@RestControllerAdvice
//@ControllerAdvice(basePackages ="com.example.demo.controller") 可指定包
public class GlobalExceptionHandler {
    @ExceptionHandler(value=GlobalException.class) //该注解声明异常处理方法
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
        e.printStackTrace();
        // 在这里针对异常做自己的处理
    }
}
```

其实SpringMVC中提供了一个异常处理的基类（`org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler`）。我们只需要将自定义的异常处理类继承这个`ResponseEntityExceptionHandler`然后复写对应的方法即可完成全局异常处理。这个类中的方法很简单，所以这里就不放代码了。

这个类中已经定义了很多的异常处理方法，如下：

```java
@ExceptionHandler({
			HttpRequestMethodNotSupportedException.class,
			HttpMediaTypeNotSupportedException.class,
			HttpMediaTypeNotAcceptableException.class,
			MissingPathVariableException.class,
			MissingServletRequestParameterException.class,
			ServletRequestBindingException.class,
			ConversionNotSupportedException.class,
			TypeMismatchException.class,
			HttpMessageNotReadableException.class,
			HttpMessageNotWritableException.class,
			MethodArgumentNotValidException.class,
			MissingServletRequestPartException.class,
			BindException.class,
			NoHandlerFoundException.class,
			AsyncRequestTimeoutException.class
})
```

所以我们只需要复写对应异常处理的方法即可完成自己在当前业务场景下异常的处理。但是需要注意的是，它只会对上面这些框架抛出的异常进行处理，对于我们自定义的异常还是会直接抛出，所以我们自定义的异常处理还是需要在其中进行定义。

#### 接口日志

我们在开发中经常会打印日志，特别是接口的入参日志，如下：

```java
@RestController
@RequestMapping("/test/simple")
@Validated
@Slf4j
public class ValidationController {

    @GetMapping("/valid")
    public String testValid(@Max(10) int age, @Valid @NotBlank String name) {
        log.info("接口入参：" + age + "      " + name);
        return "OK";
    }
}
```

如果每一个接口都需要添加这样一句代码的话就显得太LOW了，基于此我们可以使用AOP来简化代码，按照以下几步即可：

1. 自定义一个注解
2. 申明切面

##### 定义一个注解

```java
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Log {

}
```

##### 申明切面

```java
@Aspect
@Component
@Slf4j
public class LogAspect {
    @Pointcut("@annotation(com.spring.study.springfx.aop.annotation.Log)")
    private void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        String methodName = method.getName();
        Class<?> declaringClass = method.getDeclaringClass();
        String simpleName = declaringClass.getSimpleName();
        StringBuilder sb = new StringBuilder();
        sb.append(simpleName).append(".").append(methodName).append(" [");
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            sb.append(name);
            sb.append(":");
            sb.append(args[i]);
            sb.append(";");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]");
        log.info(sb.toString());
    }
}
```

基于上面的例子测试：

```java
@RestController
@RequestMapping("/test/simple")
@Validated
@Slf4j
public class ValidationController {

    @Log
    @GetMapping("/valid")
    public String testValid(@Max(10) int age, @Valid @NotBlank String name) {
        log.info("接口入参：" + age + "      " + name);
        return "OK";
    }
}

// 控制台输出日志：
// ValidationController.testValid [age:0;name:11]
```

### 总结

这篇文章到这里就结束啦，这也是《Spring官网阅读笔记》系列笔记的最后一篇。其实整个SpringFrameWork可以分为三部分

1. IOC
2. AOP
3. 事务（整合JDBC,MyBatis）

而IOC跟AOP又是整个Spring的基石，这一系列的笔记有10篇以上是IOC相关的知识。AOP的只有这一篇，这是因为Spring简化了AOP的使用，如果要探究其原理以及整个AOP的体系的话必定要深入到源码中去，所以思来想去还是决定将其放到源码阅读系列笔记中去。

# Spring官网阅读 | 总结篇



> 接近用了4个多月的时间，完成了整个《Spring官网阅读》系列的文章，本文主要对本系列所有的文章做一个总结，同时也将所有的目录汇总成一篇文章方便各位读者来阅读。

下面这张图是我整个的写作大纲![在这里插入图片描述](https://cdn.jsdelivr.net/gh/lwei20000/pic/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxOTA3OTkx,size_16,color_FFFFFF,t_70-20210128144140410.png)

对应的文章目录汇总如下：

[Spring官网阅读（一）容器及实例化](https://blog.csdn.net/qq_41907991/article/details/103589868)

> 本文主要涉及到官网中的`1.2`,`1.3`节。主要介绍了什么是容器，容器如何工作。

[Spring官网阅读（二）（依赖注入及方法注入）](https://blog.csdn.net/qq_41907991/article/details/103589884)

> 本文主要涉及到官网中的`1.4`小节，主要涉及到Spring的依赖注入

[Spring官网阅读（三）自动注入](https://blog.csdn.net/qq_41907991/article/details/103589903)

> 在对依赖注入跟方法注入有一定了解后，我们需要立马学习自动注入。通过这篇文章你会知道真正的`byName`跟`byType`。本文主要涉及到官网中的`1.4`小节

[Spring官网阅读（四）BeanDefinition（上）](https://blog.csdn.net/qq_41907991/article/details/103589939)

> 本文主要涉及到官网中的`1.3`及`1.5`中的一些补充知识。同时为我们`1.7`小节中`BeanDefinition`的合并做一些铺垫

[Spring官网阅读（五）BeanDefinition（下）](https://blog.csdn.net/qq_41907991/article/details/103866987)

> 上篇文章已经对BeanDefinition做了一系列的介绍，这篇文章我们开始学习BeanDefinition合并的一些知识，完善我们整个BeanDefinition的体系，Spring在创建一个bean时多次进行了BeanDefinition的合并，对这方面有所了解也是为以后阅读源码做准备。本文主要对应官网中的1.7小节

[Spring官网阅读（六）容器的扩展点（一）BeanFactoryPostProcessor](https://blog.csdn.net/qq_41907991/article/details/103867027)

> 之前的文章我们已经学习完了BeanDefinition的基本概念跟合并，其中多次提到了容器的扩展点，这篇文章我们就开始学习这方面的知识。这部分内容主要涉及官网中的1.8小结。按照官网介绍来说，容器的扩展点可以分类三类，BeanPostProcessor,BeanFactoryPostProcessor以及FactoryBean。本文我们主要学习BeanFactoryPostProcessor，对应官网中内容为1.8.2小节

[Spring官网阅读（七）容器的扩展点（二）FactoryBean](https://blog.csdn.net/qq_41907991/article/details/103867036)

> 在上篇文章中我们已经对容器的第一个扩展点（BeanFactoryPostProcessor）做了一系列的介绍。其中主要介绍了Spring容器中BeanFactoryPostProcessor的执行流程。已经Spring自身利用了BeanFactoryPostProcessor完成了什么功能，对于一些细节问题可能说的不够仔细，但是在当前阶段我想要做的主要是为我们以后学习源码打下基础。所以对于这些问题我们暂且不去过多纠结，待到源码学习阶段我们会进行更加细致的分析。
>
> 在本篇文章中，我们将要学习的是容器的另一个扩展点（FactoryBean）,对于FactoryBean官网上的介绍甚短，但是如果我们对Spring的源码有一定了解，可以发现Spring在很多地方都对这种特殊的Bean做了处理。话不多说，我们开始进入正文。

[Spring官网阅读（八）容器的扩展点（三）（BeanPostProcessor）](https://blog.csdn.net/qq_41907991/article/details/103867048)

> 在前面两篇关于容器扩展点的文章中，我们已经完成了对BeanFactoryPostProcessor很FactoryBean的学习，对于BeanFactoryPostProcessor而言，它能让我们对容器中的扫描出来的BeanDefinition做出修改以达到扩展的目的，而对于FactoryBean而言，它提供了一种特殊的创建Bean的手段，能让我们将一个对象直接放入到容器中，成为Spring所管理的一个Bean。而我们今天将要学习的BeanPostProcessor不同于上面两个接口，它主要干预的是Spring中Bean的整个生命周期（实例化—属性填充—初始化—销毁），关于Bean的生命周期将在下篇文章中介绍，如果不熟悉暂且知道这个概念即可，下面进入我们今天的正文。

[Spring官网阅读（九）Spring中Bean的生命周期（上）](https://blog.csdn.net/qq_41907991/article/details/104786530)

> 在之前的文章中，我们一起学习过了官网上容器扩展点相关的知识，包括FactoryBean，BeanFactroyPostProcessor,BeanPostProcessor，其中BeanPostProcessor还剩一个很重要的知识点没有介绍，就是相关的BeanPostProcessor中的方法的执行时机。之所以在之前的文章中没有介绍是因为这块内容涉及到Bean的生命周期。在这篇文章中我们开始学习Bean的生命周期相关的知识，整个Bean的生命周期可以分为以下几个阶段：
>
> 实例化（得到一个还没有经过属性注入跟初始化的对象）
> 属性注入（得到一个经过了属性注入但还没有初始化的对象）
> 初始化（得到一个经过了初始化但还没有经过AOP的对象，AOP会在后置处理器中执行）
> 销毁
> 在上面几个阶段中，BeanPostProcessor将会穿插执行。而在初始化跟销毁阶段又分为两部分：
>
> 生命周期回调方法的执行
> aware相关接口方法的执行
> 这篇文章中，我们先完成Bean生命周期中，整个初始化阶段的学习，对于官网中的章节为1.6小结

[Spring官网阅读（十）Spring中Bean的生命周期（下）](https://blog.csdn.net/qq_41907991/article/details/104786584)

> 在上篇文章中，我们已经对Bean的生命周期做了简单的介绍，主要介绍了整个生命周期中的初始化阶段以及基于容器启动停止时LifeCycleBean的回调机制，另外对Bean的销毁过程也做了简单介绍。但是对于整个Bean的生命周期，这还只是一小部分，在这篇文章中，我们将学习完成剩下部分的学习，同时对之前的内容做一次复习。整个Bean的生命周期，按照我们之前的介绍，可以分为四部分
>
> 实例化
> 属性注入
> 初始化
> 销毁
> 本文主要介绍实例化及属性注入阶段

[Spring官网阅读（十一）ApplicationContext详细介绍（上）](https://blog.csdn.net/qq_41907991/article/details/104890350)

> 在前面的文章中，我们已经完成了官网中关于IOC内容核心的部分。包括容器的概念，Spring创建Bean的模型BeanDefinition的介绍，容器的扩展点（BeanFactoryPostProcessor，FactroyBean，BeanPostProcessor）以及最重要的Bean的生命周期等。接下来大概还要花三篇文章完成对官网中第一大节的其它内容的学习，之所以要这么做，是笔者自己粗读了一篇源码后，再读一遍官网，发现源码中的很多细节以及难点都在官网中介绍了。所以这里先跟大家一起把官网中的内容都过一遍，也是为了更好的进入源码学习阶段。
>
> 本文主要涉及到官网中的1.13,1.15,1.16小节中的内容以及第二大节的内容

[Spring官网阅读（十二）ApplicationContext详解（中）](https://blog.csdn.net/qq_41907991/article/details/104934770)

> 在上篇文章中我们已经对ApplicationContext的一部分内容做了介绍，ApplicationContext主要具有以下几个核心功能：
>
> 国际化
> 借助Environment接口，完成了对Spring运行环境的抽象，可以返回环境中的属性，并能出现出现的占位符
> 借助于Resource系列接口，完成对底层资源的访问及加载
> 继承了ApplicationEventPublisher接口，能够进行事件发布监听
> 负责创建、配置及管理Bean
> 在上篇文章我们已经分析学习了1，2两点，这篇文章我们继续之前的学习

[Spring官网阅读（十三）ApplicationContext详解（下）](https://blog.csdn.net/qq_41907991/article/details/105197581)

> 在前面两篇文章中，我们已经对ApplicationContext的大部分内容做了介绍，包括国际化，Spring中的运行环境，Spring中的资源，Spring中的事件监听机制，还剩唯一一个BeanFactory相关的内容没有介绍，这篇文章我们就来介绍BeanFactory，这篇文章结束，关于ApplicationContext相关的内容我们也总算可以告一段落了。本文对应官网中的1.16及1.15小结

[Spring官网阅读（十四）Spring中的BeanWrapper及类型转换](https://blog.csdn.net/qq_41907991/article/details/105214244)

> BeanWrapper是Spring中一个很重要的接口，Spring在通过配置信息创建对象时，第一步首先就是创建一个BeanWrapper。这篇文章我们就分析下这个接口，本文内容主要对应官网中的`3.3`及`3.4`小结

[Spring官网阅读（十五）Spring中的格式化（Formatter）](https://blog.csdn.net/qq_41907991/article/details/105237926)

> 在上篇文章中，我们已经学习过了Spring中的类型转换机制。现在我们考虑这样一个需求：在我们web应用中，我们经常需要将前端传入的字符串类型的数据转换成指定格式或者指定数据类型来满足我们调用需求，同样的，后端开发也需要将返回数据调整成指定格式或者指定类型返回到前端页面。这种情况下，Converter已经没法直接支撑我们的需求了。这个时候，格式化的作用就很明显了，这篇文章我们就来介绍Spring中格式化的一套体系。本文主要涉及官网中的3.5及3.6小结

[Spring官网阅读（十六）Spring中的数据绑定](https://blog.csdn.net/qq_41907991/article/details/105333258)

> 在前面的文章我们学习过了Spring中的类型转换以及格式化，对于这两个功能一个很重要的应用场景就是应用于我们在XML中配置的Bean的属性值上，如下：
>
>   1 2 3 4 5 在上面这种情况下，我们从XML中解析出来的值类型肯定是String类型，而对象中的属性为int类型，当Spring将配置中的数据应用到Bean上时，就调用了我们的类型转换器完成了String类型的字面值到int类型的转换。
>
>  
>
> 那么除了在上面这种情况中使用了类型转换，还有哪些地方用到了呢？对了，就是本文要介绍的数据绑定–DataBinder。

[Spring官网阅读（十七）Spring中的数据校验](https://blog.csdn.net/qq_41907991/article/details/105337481)

> 在前文中我们一起学习了Spring中的数据绑定，也就是整个DataBinder的体系，其中有提到DataBinder跟校验相关。可能对于Spring中的校验大部分同学跟我一一样，都只是知道可以通过@Valid / @Validated来对接口的入参进行校验，但是对于其底层的具体实现以及一些细节都不是很清楚，通过这篇文章我们就来彻底搞懂Spring中的校验机制。
>
> 在学习Spring中某个功能时，往往要从Java本身出发。比如我们之前介绍过的Spring中的国际化（见《Spring官网阅读（十一）》）、Spring中的ResolvableType（见《Spring杂谈》系列文章）等等，它们都是对Java本身的封装，沿着这个思路，我们要学习Spring中的数据校验，必然要先对Java中的数据校验有一定了解。

[Spring官网阅读（十八）Spring中的AOP ](https://blog.csdn.net/qq_41907991/article/details/105500421)

> 本篇文章将作为整个Spring官网阅读笔记的最后一篇。如果要谈SpringFramework必定离不开两点
>
> - IOC（控制反转）
> - AOP（面向切面）
>   在前面的文章中我们已经对IOC做过详细的介绍了，本文主要介绍AOP，关于其中的源码部分将在专门的源码专题介绍，本文主要涉及的是AOP的基本概念以及如何使用,本文主要涉及到官网中的第5、6两大章

要是文章有帮助到你的话，记得点个赞吧！~













