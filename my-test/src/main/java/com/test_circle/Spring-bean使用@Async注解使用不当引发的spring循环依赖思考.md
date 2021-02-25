# [@Async注解使用不当引发的spring循环依赖思考](https://segmentfault.com/a/1190000021217176)

最近项目启动的时候疯狂抛错，具体如下:

```
org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'classA': Bean with name 'classA' has been injected into other beans [classB] in its raw version as part of a circular reference, but has eventually been wrapped. This means that said other beans do not use the final version of the bean. This is often the result of over-eager type matching - consider using 'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.
```

大概意思是：创建名为“classA”的bean时出错：名为“classA”的bean已作为循环引用的一部分被注入其原始版本中的其他bean[classB]，但最终已被包装。后来发现，是在一个方法上面添加了@Async注解导致的，去掉此注解后，项目得以正常启动。

> **循环依赖分析**

我们知道，spring注入bean的方式有三种：

```
1. 构造器注入
2. setter
3. 自动注入
```

但是我们使用spring的时候，默认Spring是会给我们解决循环依赖的，为什么呢？他使用的是哪种方式？为什么加了一个注解@Async的时候，循环依赖就失效了呢？这些问题我们一点点的来分析。
为了方便分析，我写了一个最简单的小demo来说明此事，这个demo只有两个类，ClassA和ClassB,其中A,B互相注入对方。如果不使用@Async我们会发现项目可以启动，spring的成功的帮助我们解决了循环依赖的问题。但是加入了@Async以后项目启动不出来，抛出前面我们所说的异常.下面是两个依赖的关系：

```
/**
* create by liuliang
 * on 2019-12-06  11:21
 */
@Component
public class ClassA implements InterfaceA{
     @Autowired
    private InterfaceB b;

    @Async
    @Override
    public void testA() {

    }
}
```

------

```
/**
 * create by liuliang
 * on 2019-12-06  11:22
 */
@Component
public class ClassB implements InterfaceB{
    @Autowired
    private InterfaceA a;

    @Override
    public void testB() {
        a.testA();
    }
}
```

根据抛错，我们定位一下异常错误的地方：

```
AbstractAutowireCapableBeanFactory.java
```

```
protected Object doCreateBean( ... ){
...
boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences && isSingletonCurrentlyInCreation(beanName));
if (earlySingletonExposure) {
    addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
}
...

// populateBean这一句特别的关键，它需要给A的属性赋值，所以此处会去实例化B~~
// 而B我们从上可以看到它就是个普通的Bean（并不需要创建代理对象），实例化完成之后，继续给他的属性A赋值，而此时它会去拿到A的早期引用
// 也就在此处在给B的属性a赋值的时候，会执行到上面放进去的Bean A流程中的getEarlyBeanReference()方法  从而拿到A的早期引用~~
// 执行A的getEarlyBeanReference()方法的时候，会执行自动代理创建器，但是由于A没有标注事务，所以最终不会创建代理，so B合格属性引用会是A的**原始对象**
// 需要注意的是：@Async的代理对象不是在getEarlyBeanReference()中创建的，是在postProcessAfterInitialization创建的代理
// 从这我们也可以看出@Async的代理它默认并不支持你去循环引用，因为它并没有把代理对象的早期引用提供出来~~~（注意这点和自动代理创建器的区别~）
// 结论：此处给A的依赖属性字段B赋值为了B的实例(因为B不需要创建代理，所以就是原始对象)
// 而此处实例B里面依赖的A注入的仍旧为Bean A的普通实例对象（注意：是原始对象非代理对象。注：此时exposedObject也依旧为原始对象）
populateBean(beanName, mbd, instanceWrapper);

// 标注有@Async的Bean的代理对象在此处会被生成~~~ 参照类：AsyncAnnotationBeanPostProcessor
// 所以此句执行完成后  exposedObject就会是个代理对象而非原始对象了
exposedObject = initializeBean(beanName, exposedObject, mbd);

...
// 这里是报错的重点~~~
if (earlySingletonExposure) {
    // 上面说了A被B循环依赖进去了，所以此时A是被放进了二级缓存的，所以此处earlySingletonReference 是A的原始对象的引用
    // （这也就解释了为何我说：如果A没有被循环依赖，是不会报错不会有问题的   因为若没有循环依赖earlySingletonReference =null后面就直接return了）
    Object earlySingletonReference = getSingleton(beanName, false);
    if (earlySingletonReference != null) {
        // 上面分析了exposedObject 是被@Aysnc代理过的对象， 而bean是原始对象 所以此处不相等  走else逻辑
        if (exposedObject == bean) {
            exposedObject = earlySingletonReference;
        }
        // allowRawInjectionDespiteWrapping 标注是否允许此Bean的原始类型被注入到其它Bean里面，即使自己最终会被包装（代理）
        // 默认是false表示不允许，如果改为true表示允许，就不会报错啦。这是我们后面讲的决方案的其中一个方案~~~
        // 另外dependentBeanMap记录着每个Bean它所依赖的Bean的Map~~~~
        else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
            // 我们的Bean A依赖于B，so此处值为["b"]
            String[] dependentBeans = getDependentBeans(beanName);
            Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);

            // 对所有的依赖进行一一检查~    比如此处B就会有问题
            // “b”它经过removeSingletonIfCreatedForTypeCheckOnly最终返返回false  因为alreadyCreated里面已经有它了表示B已经完全创建完成了~~~
            // 而b都完成了，所以属性a也赋值完成儿聊 但是B里面引用的a和主流程我这个A竟然不相等，那肯定就有问题(说明不是最终的)~~~
            // so最终会被加入到actualDependentBeans里面去，表示A真正的依赖~~~
            for (String dependentBean : dependentBeans) {
                if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                    actualDependentBeans.add(dependentBean);
                }
            }

            // 若存在这种真正的依赖，那就报错了~~~  则个异常就是上面看到的异常信息
            if (!actualDependentBeans.isEmpty()) {
                throw new BeanCurrentlyInCreationException(beanName,
                        "Bean with name '" + beanName + "' has been injected into other beans [" +
                        StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                        "] in its raw version as part of a circular reference, but has eventually been " +
                        "wrapped. This means that said other beans do not use the final version of the " +
                        "bean. This is often the result of over-eager type matching - consider using " +
                        "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
            }
        }
    }
}
...
}
```

这里知识点避开不@Aysnc注解标注的Bean的创建代理的时机。
@EnableAsync开启时它会向容器内注入AsyncAnnotationBeanPostProcessor，它是一个BeanPostProcessor，实现了postProcessAfterInitialization方法。此处我们看代码，创建代理的动作在抽象父类AbstractAdvisingBeanPostProcessor上：

```
// @since 3.2   注意：@EnableAsync在Spring3.1后出现
// 继承自ProxyProcessorSupport，所以具有动态代理相关属性~ 方便创建代理对象
public abstract class AbstractAdvisingBeanPostProcessor extends ProxyProcessorSupport implements BeanPostProcessor {

// 这里会缓存所有被处理的Bean~~~  eligible：合适的
private final Map<Class<?>, Boolean> eligibleBeans = new ConcurrentHashMap<>(256);

//postProcessBeforeInitialization方法什么不做~
@Override
public Object postProcessBeforeInitialization(Object bean, String beanName) {
    return bean;
}

// 关键是这里。当Bean初始化完成后这里会执行，这里会决策看看要不要对此Bean创建代理对象再返回~~~
@Override
public Object postProcessAfterInitialization(Object bean, String beanName) {
    if (this.advisor == null || bean instanceof AopInfrastructureBean) {
        // Ignore AOP infrastructure such as scoped proxies.
        return bean;
    }

    // 如果此Bean已经被代理了（比如已经被事务那边给代理了~~）
    if (bean instanceof Advised) {
        Advised advised = (Advised) bean;
    
        // 此处拿的是AopUtils.getTargetClass(bean)目标对象，做最终的判断
        // isEligible()是否合适的判断方法  是本文最重要的一个方法，下文解释~
        // 此处还有个小细节：isFrozen为false也就是还没被冻结的时候，就只向里面添加一个切面接口   并不要自己再创建代理对象了  省事
        if (!advised.isFrozen() && isEligible(AopUtils.getTargetClass(bean))) {
            // Add our local Advisor to the existing proxy's Advisor chain...
            // beforeExistingAdvisors决定这该advisor最先执行还是最后执行
            // 此处的advisor为：AsyncAnnotationAdvisor  它切入Class和Method标注有@Aysnc注解的地方~~~
            if (this.beforeExistingAdvisors) {
                advised.addAdvisor(0, this.advisor);
            } else {
                advised.addAdvisor(this.advisor);
            }
            return bean;
        }
    }

    // 若不是代理对象，此处就要下手了~~~~isEligible() 这个方法特别重要
    if (isEligible(bean, beanName)) {
        // copy属性  proxyFactory.copyFrom(this); 生成一个新的ProxyFactory 
        ProxyFactory proxyFactory = prepareProxyFactory(bean, beanName);
        // 如果没有强制采用CGLIB 去探测它的接口~
        if (!proxyFactory.isProxyTargetClass()) {
            evaluateProxyInterfaces(bean.getClass(), proxyFactory);
        }
        // 添加进此切面~~ 最终为它创建一个getProxy 代理对象
        proxyFactory.addAdvisor(this.advisor);
        //customize交给子类复写（实际子类目前都没有复写~）
        customizeProxyFactory(proxyFactory);
        return proxyFactory.getProxy(getProxyClassLoader());
    }

    // No proxy needed.
    return bean;
}

// 我们发现BeanName最终其实是没有用到的~~~
// 但是子类AbstractBeanFactoryAwareAdvisingPostProcessor是用到了的  没有做什么 可以忽略~~~
protected boolean isEligible(Object bean, String beanName) {
    return isEligible(bean.getClass());
}
protected boolean isEligible(Class<?> targetClass) {
    // 首次进来eligible的值肯定为null~~~
    Boolean eligible = this.eligibleBeans.get(targetClass);
    if (eligible != null) {
        return eligible;
    }
    // 如果根本就没有配置advisor  也就不用看了~
    if (this.advisor == null) {
        return false;
    }
    
    // 最关键的就是canApply这个方法，如果AsyncAnnotationAdvisor  能切进它  那这里就是true
    // 本例中方法标注有@Aysnc注解，所以铁定是能被切入的  返回true继续上面方法体的内容
    eligible = AopUtils.canApply(this.advisor, targetClass);
    this.eligibleBeans.put(targetClass, eligible);
    return eligible;
}
...
}
```

经此一役，根本原理是只要能被切面AsyncAnnotationAdvisor切入（即只需要类/方法有标注@Async注解即可）的Bean最终都会生成一个代理对象（若已经是代理对象里，只需要加入该切面即可了~）赋值给上面的exposedObject作为返回最终add进Spring容器内~

针对上面的步骤，为了辅助理解，我尝试总结文字描述如下：

1.context.getBean(A)开始创建A，A实例化完成后给A的依赖属性b开始赋值~
2.context.getBean(B)开始创建B，B实例化完成后给B的依赖属性a开始赋值~
3.重点：此时因为A支持循环依赖，所以会执行A的getEarlyBeanReference方法得到它的早期引用。而执行getEarlyBeanReference()的时候因为@Async根本还没执行，所以最终返回的仍旧是原始对象的地址
4.B完成初始化、完成属性的赋值，此时属性field持有的是Bean A原始类型的引用~
5.完成了A的属性的赋值（此时已持有B的实例的引用），继续执行初始化方法initializeBean(...)，在此处会解析@Aysnc注解，从而生成一个代理对象，所以最终exposedObject是一个代理对象（而非原始对象）最终加入到容器里~
6.尴尬场面出现了：B引用的属性A是个原始对象，而此处准备return的实例A竟然是个代理对象，也就是说B引用的并非是最终对象（不是最终放进容器里的对象）
7.执行自检程序：由于allowRawInjectionDespiteWrapping默认值是false，表示不允许上面不一致的情况发生，so最终就抛错了~

以上是自己的理解加上网上参考资料所得，如有问题，还望各位极力斧正！
参考资料：
https://blog.csdn.net/f641385712/article/details/92797058