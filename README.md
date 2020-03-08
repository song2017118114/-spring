# easySpring
## 目录：
- [easySpring](#easyspring)
    - [1. 简单的IOC和AOP的实现](#1-简单的ioc和aop的实现)
        - [1.1 简单的IOC容器实现](#11-简单的ioc容器实现)
        - [1.2 简单的 AOP 实现](#12-简单的-aop-实现)
            - [AOP的基本概念](#aop的基本概念)
            - [动态代理概念](#动态代理概念)
            - [简单AOP基本流程：](#简单aop基本流程)
    - [2. 复杂的IOC和AOP实现](#2-复杂的ioc和aop实现)
        - [2.1 IOC的实现](#21-ioc的实现)
            - [2.1.1 BeanFactory的生命流程](#211-beanfactory的生命流程)
            - [2.1.2 xml 的解析](#212-xml-的解析)
            - [2.1.3 注册BeanPostProcessor](#213-注册beanpostprocessor)
            - [2.1.4 getBean过程解析](#214-getbean过程解析)
            - [代码解析：](#代码解析)
        - [2.2 AOP的实现](#22-aop的实现)
            - [2.2.1 基于 JDK 动态代理的 AOP 实现](#221-基于-jdk-动态代理的-aop-实现)
        - [2.3 AOP 与 IOC 协作](#23-aop-与-ioc-协作)


在学到Java的Spring容器概念这里，我参考网上的博客和自己的理解实现了一个简易的Spring容器也就是这个项目。该项目分为`up`和`down`两部分。

`up`模块只实现了最简单的IOC和AOP功能，两者并没有联系起来。

后来在`down`中实现了更复杂的IOC、AOP功能，并使两者能够很好地进行协作。

## 1. 简单的IOC和AOP的实现

### 1.1 简单的IOC容器实现

先从简单的 IOC 容器实现开始，最简单的 IOC 容器只需4步即可实现，如下：

1. 加载 xml 配置文件，遍历其中的 <bean> 标签
2. 获取<bean>标签中的 id 和 class 属性，加载 class 属性对应的类，并创建 bean
3. 遍历 <bean> 标签中的 <property> 标签，获取属性值，并将属性值填充到 bean 中
4. 将 bean 注册到 bean 容器中

代码结构如下：

```java
SimpleIOC     // IOC 的实现类，实现了上面所说的4个步骤
SimpleIOCTest // IOC 的测试类
Car           // IOC 测试使用的 bean
Wheel         // 同上 
ioc.xml       // bean 配置文件
```

容器实现类 SimpleIOC 的代码： 

```java
package easySpring.up.ioc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class SimpleIOC {
    private Map<String,Object> beanMap = new HashMap<>();

    public SimpleIOC(String location) throws Exception{
        loadBeans(location);
    }

    private void loadBeans(String location) throws Exception{
        //加载XML配置文件
        InputStream inputStream = new FileInputStream(location);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        Element root = document.getDocumentElement();
        NodeList nodes = root.getChildNodes();

        // 遍历 <bean> 标签
        for (int i = 0;i < nodes.getLength();i++){
            Node node = nodes.item(i);
            if (node instanceof  Element){
                Element element = (Element)node;
                String id = element.getAttribute("id");
                String className = element.getAttribute("class");

                //加载beanClass
                Class beanClass = null;
                try {
                    beanClass = Class.forName(className);//通过反射加载Class
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                    return;
                }

                //创建bean
                Object bean = beanClass.newInstance();

                // 遍历 <property> 标签
                NodeList propertyNodes = element.getElementsByTagName("property");
                for (int j = 0;j < propertyNodes.getLength();j++){
                    Node propertyNode = propertyNodes.item(j);
                    if (propertyNode instanceof Element){
                        Element propertyElement = (Element) propertyNode;
                        String name = propertyElement.getAttribute("name");
                        String value = propertyElement.getAttribute("value");

                        // 利用反射将 bean 相关字段访问权限设为可访问
                        // getDeclaredField获取所有声明的成员变量，不管是public还是private
                        Field declaredField = bean.getClass().getDeclaredField(name);
                        declaredField.setAccessible(true);//针对私有变量而言

                        if (value != null && value.length() > 0){
                            //将属性填充到相关字段中
                            declaredField.set(bean,value);
                        }else{
                            String ref = propertyElement.getAttribute("ref");
                            if (ref == null || ref.length() == 0){
                                throw new IllegalArgumentException("ref config error");
                            }

                            //将引用填充到相应字段中
                            declaredField.set(bean,getBean(ref));
                        }

                        // 将 bean 注册到 bean 容器中
                        registerBean(id, bean);

                    }
                }
            }
        }
    }

    /**
     * 获取bean
     * @param
     * @return
     */
    public Object getBean(String name) {
        Object bean = beanMap.get(name);
        if (bean == null){
            throw new IllegalArgumentException("there is no bean with name "+ name);
        }
        return bean;
    }

    /**
     * 注册到bean容器中
     * @param id
     * @param bean
     */
    private void registerBean(String id, Object bean) {

        beanMap.put(id,bean);
    }

}

```

bean的domain代码：

```java
public class Car {
    private String name;
    private String length;
    private String width;
    private String height;
    private Wheel wheel;
    
    // 省略其他不重要代码
}

public class Wheel {
    private String brand;
    private String specification ;
    
    // 省略其他不重要代码
}
```

配置文件iox.xml：

```xml
<beans>
    <bean id="wheel" class="easySpring.up.ioc.Wheel">
        <property name="brand" value="Michelin" />
        <property name="specification" value="265/60 R18" />
    </bean>

    <bean id="car" class="easySpring.up.ioc.Car">
        <property name="name" value="Mercedes Benz G 500"/>
        <property name="length" value="4717mm"/>
        <property name="width" value="1855mm"/>
        <property name="height" value="1949mm"/>
        <property name="wheel" ref="wheel"/>
    </bean>
</beans>
```

测试类：

```java
public class SimpleIOCTest {
    @Test
    public void getBean() throws Exception {
        String location = SimpleIOC.class.
            getClassLoader().getResource("ioc.xml").getFile();
        SimpleIOC bf = new SimpleIOC(location);
        Wheel wheel = (Wheel) bf.getBean("wheel");
        System.out.println(wheel);
        Car car = (Car) bf.getBean("car");
        System.out.println(car);
    }
}
```

运行结果：

```java
Wheel{brand='Michelin', specification='265/60 R18'}
Car{name='Mercedes Benz G 500', length='4717mm', width='1855mm', height='1949mm', wheel=Wheel{brand='Michelin', specification='265/60 R18'}}

Process finished with exit code 0
```

### 1.2 简单的 AOP 实现

先说一些AOP的基本概念吧：

#### AOP的基本概念

通知（Advice）

```java
通知定义了要织入目标对象的逻辑，以及执行时机。
    Spring 中对应了 5 种不同类型的通知：
    · 前置通知（Before）：在目标方法执行前，执行通知
    · 后置通知（After）：在目标方法执行后，执行通知，此时不关系目标方法返回的结果是什么
    · 返回通知（After-returning）：在目标方法执行后，执行通知
    · 异常通知（After-throwing）：在目标方法抛出异常后执行通知
    · 环绕通知（Around）: 目标方法被通知包裹，通知在目标方法执行前和执行后都被会调用
```

切点（Pointcut）

```
    如果说通知定义了在何时执行通知，那么切点就定义了在何处执行通知。所以切点的作用就是
    通过匹配规则查找合适的连接点（Joinpoint），AOP 会在这些连接点上织入通知。
```

切面（Aspect）

```
切面包含了通知和切点，通知和切点共同定义了切面是什么，在何时，何处执行切面逻辑。
```

这里的简单AOP是基于动态代理实现的，先来了解一下吧：

#### 动态代理概念

**静态代理**主要通过将目标类与代理类**实现同一个接口**，让代理类持有真实类对象，然后在代理类方法中调用真实类方法，在调用真实类方法的前后添加我们所需要的功能扩展代码来达到增强的目的。

静态代理会为每一个业务增强都提供一个代理类, 由代理类来创建代理对象, 而**动态代理并不存在代理类, 代理对象直接由代理生成工具动态生成**。

与静态代理相比，**动态代理**的代理类不需要程序员自己手动定义，而是在程序运行时动态生成。

> **动态代理**可以分为JDK动态代理和CgLib动态代理
>
> ##### JDK动态代理
>
> JDK动态代理与静态代理一样，目标类需要**实现一个代理接口**，它的开发步骤如下：
>  1.定义一个java.lang.reflect.InvocationHandler接口的实现类，重写invoke方法
>  2.将InvocationHandler对象作为参数传入java.lang.reflect.Proxy的newProxyInstance方法中
>  3.通过调用java.lang.reflect.Proxy的newProxyInstance方法获得动态代理对象
>  4.通过代理对象调用目标方法
>
> JDK动态代理和CgLib动态代理的**主要区别**：
>  JDK动态代理只能针对**实现了接口**的类的接口方法进行代理
>  CgLib动态代理基于继承来实现代理，所以无法对final类、private方法和static方法实现代理
>
> Spring AOP中的代理使用的**默认策略是**：
>  如果目标对象实现了接口，则默认采用JDK动态代理
>  如果目标对象没有实现接口，则采用CgLib进行动态代理
>  如果目标对象实现了接口，且强制CgLib代理，则采用CgLib进行动态代理

#### 简单AOP基本流程：

这里 AOP 是基于 JDK 动态代理实现的，只需3步即可完成：

1. 定义一个包含切面逻辑的对象，这里假设叫 logTask

2. 定义一个 BeforeAdvice 对象（间接实现了 InvocationHandler 接口），并将上面的 logTask 和 目标对象传入

3. 将上面的 BeforeAdvice对象和目标对象传给 **JDK 动态代理方法**，为目标对象生成代理

代码结构：

```java
MethodInvocation 接口  // 实现类包含了切面逻辑，如上面的 logTask
Advice 接口        // 继承了 InvocationHandler 接口
BeforeAdvice 类    // 实现了 Advice 接口，是一个前置通知
SimpleAOP 类       // 生成代理类
SimpleAOPTest      // SimpleAOP 从测试类
HelloService 接口   // 目标对象接口
HelloServiceImpl   // 目标对象
```

画一个UML图来理解：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200308165242186.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNzYyMDI3,size_16,color_FFFFFF,t_70)

MethodInvocation 接口代码：

```java
/**
 实现类包含了切面逻辑
 */
public interface MethodInvocation {
    void invoke();
}
```

Advice 接口代码：

```java
/**
 * 继承了 InvocationHandler 接口
 */
public interface Advice extends InvocationHandler {}
```

BeforeAdvice 实现代码：

```java
/**
 * 实现了 Advice 接口，是一个前置通知
 */
public class BeforeAdvice implements Advice {
    private Object bean;
    private MethodInvocation methodInvocation;

    public BeforeAdvice(Object bean, MethodInvocation methodInvocation) {
        this.bean = bean;
        this.methodInvocation = methodInvocation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 在目标方法执行前调用通知
        methodInvocation.invoke();
        return method.invoke(bean, args);
    }
}
```

SimpleAOP 实现代码：

```java
public class SimpleAOP {

    public static Object getProxyInstance(Object bean,Advice advice){
        return Proxy.newProxyInstance(
                SimpleAOP.class.getClassLoader(),//用哪个类加载器去加载代理对象
                bean.getClass().getInterfaces(),//动态代理类需要实现的接口
                advice);//传入的InvocationHandler，用这个来增强
    }
}
```

HelloService 接口，及其实现类代码：

```java
public interface HelloService {
    void sayHelloWorld();
}

public class HelloServiceImpl implements HelloService {
    @Override
    public void sayHelloWorld() {
        System.out.println("hello world!");
    }
}
```

SimpleAOPTest 代码:

```java
public class SimpleAOPTest {

    @Test
    public void getProxy() throws Exception{
        //1. 创建一个 MethodInvocation 实现类，包含了切面逻辑
        MethodInvocation logTask = ()-> System.out.println("log task start");
        //被代理的对象
        HelloServiceImpl helloServiceImpl = new HelloServiceImpl();

        //2. 创建一个Advice，传入被代理对象和切面逻辑，完成增强
        Advice beforeAdvice = new BeforeAdvice(helloServiceImpl,logTask);

        //3.为目标对象生成代理
        //运行时创建接口的动态实现
        HelloService helloServiceImplProxy = (HelloService) SimpleAOP.getProxyInstance(
                helloServiceImpl,//增强的对象
                beforeAdvice//通知，相当于把Proxy与InvocationHandler连起来了
        );

        helloServiceImplProxy.sayHelloWorld();
    }
}
```

运行结果：

```java
log task start
hello world!

Process finished with exit code 0
```

此时实现的 IOC 和 AOP 还很简单，且只能独立运行。在节中，我将实现一个较为复杂的 IOC 和 AOP，并且能够很好地协作。

## 2. 复杂的IOC和AOP实现

这一版本的easySpring更强大，实现的功能有：

1. 根据 xml 配置文件加载相关 bean
2. 对 BeanPostProcessor 类型的 bean 提供支持
3. 对 BeanFactoryAware 类型的 bean 提供支持
4. 实现了基于 JDK 动态代理的 AOP
5. 整合了 IOC 和 AOP，使得二者可很好的协同工作

### 2.1 IOC的实现

#### 2.1.1 BeanFactory的生命流程

1. BeanFactory加载Bean配置文件，将读到的Bean配置**封装成BeanDefinition**对象
2. 将封装好的BeanDefinition对象注册到**BeanDefinition容器**中
3. 注册BeanPostProcessor相关实现类到**BeanPostProcessor容器**中
4. BeanFactory进入**就绪**状态
5. 外部调用BeanFactory的**getBean(String name)**方法，BeanFactory着手**实例化相应的bean**
6. 重复3、4，直至程序退出，BeanFactory被销毁

**辅助类：**

**BeanDefinition**：相当于把读取的配置文件生成的配置清单，id、class、properties、ref都有

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200308165320251.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNzYyMDI3,size_16,color_FFFFFF,t_70)

```java
package easySpring.down.ioc;

public class BeanDefinition {

    private Object bean;

    private Class beanClass;

    private String beanClassName;//全限定类名

    private PropertyValues propertyValues = new PropertyValues();//属性列表

    public BeanDefinition(){
    }


    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Object getBean() {
        return bean;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    /**
     * 改了名字，也要反射通过名字改变成对应的Class
     * @param beanClassName
     */
    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
        try {
            this.beanClass = Class.forName(beanClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public PropertyValues getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }
}

```

接下来看描述属性的类：

**PropertyValue** 中有两个字段 name 和 value，用于记录 bean 配置中的标签的属性值。然后是**PropertyValues**，PropertyValues 从字面意思上来看，是 PropertyValue 复数形式，在功能上等同于 List。那么为什么 Spring 不直接使用 List，而自己定义一个新类呢？答案是**要获得一定的控制权**，看下面的代码： 

```java
public class PropertyValues {

    private final List<PropertyValue> propertyValueList = new ArrayList<>();

    public void addPropertyValue(PropertyValue pv){
        //在这里可以对参数值pv做一些处理
        this.propertyValueList.add(pv);
    }

    public List<PropertyValue> getPropertyValueList(){
        return this.propertyValueList;
    }
}
```

#### 2.1.2 xml 的解析

BeanFactory初始化时，会根据传入的xml配置文件路径来加载配置文件，然而BeanFactory值需要管理容器中的bean就可以了，加载和解析配置文件的任务由BeanDefinitionReader的实现类**XmlBeanDefinitionReader**去做就可以了

1. 将xml配置文件加载到**内存**中
2. 获取根标签下的所有标签
3. 遍历获取到的标签列表，取出id、class属性
4. **创建BeanDefinition对象**，并将刚刚读取到的id、class属性保存到对象中
5. 遍历标签下的标签，读取属性值并保存到对象中
6. 将<id,BeanDefinition>**键值对缓存在Map中**，留在后面使用
7. 重复3、4、5、6步，直至解析结束

#### 2.1.3 注册BeanPostProcessor

BeanPostProcessor是Spring对外拓展的接口之一，主要用途：提供一个机会，让开发人员能够**插手bean的实例化过程。我们可以在bean实例化的过程中对bean进行一些处理，比如AOP织入相关bean中**。

康康BeanFactory如何注册BeanPostProcessor相关实现类的

XmlBeanDefinitionReader在完成解析工作后，BeanFactory会将键值对<id,BeanDefinition>注册到自己的**beanDefinitionMap**中。BeanFactory注册好**BeanDefinition**后，就立即开始注册BeanPostProcessor的相关实现类。这个过程

1. 根据BeanDefinition记录的信息，寻找所有**实现了BeanPostProcessor的接口的类**
2. 实例化BeanPostProcessor接口的实现类
3. 将实例化好的对象放入**List**中
4. 重复2、3步，直至所有的实现类完成注册

#### 2.1.4 getBean过程解析

在完成xml的解析，BeanDefinition的注册，以及BeanPostProcessor的注册后，BeanFactory的初始化工作就算是结束了，此时BeanFactory处于就绪状态，等待外部程序的调用。它具有**延迟实例化bean的特性**，也就是等外部程序需要的时候，才实例化。这样做

- 提高BeanFactory的初始化速度
- 节省内存资源

看看Spring bean实例化过程 ：

 ![bean实例化过程](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9ibG9nLXBpY3R1cmVzLm9zcy1jbi1zaGFuZ2hhaS5hbGl5dW5jcy5jb20vYmVhbiVlNSVhZSU5ZSVlNCViZSU4YiVlNSU4YyU5NiVlOCViZiU4NyVlNyVhOCU4Yi5wbmc?x-oss-process=image/format,png)

本项目中，实例化流程被简化：

> 1. 实例化bean对象
> 2. 设置对象属性： 将配置文件中配置的属性填充到刚刚创建的 bean 对象中 
> 3. 检查Aware相关接口并设置相关依赖
> 4. BeanPostProcessor前置处理 postProcessBeforeInitialization(Object bean, String beanName) 
> 5. BeanPostProcessor后置处理 postProcessAfterInitialization(Object bean, String beanName) 
> 6. 使用中

#### 代码解析：

看看包含主要逻辑的类：XmlBeanFactory

```java
public class XmlBeanFactory implements BeanFactory {

    //bean容器
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    //使用它间接读取配置文件
    private XmlBeanDefinitionReader beanDefinitionReader;

    //存放beanDefinition的名字的List
    private List<String> beanDefinitionNames = new ArrayList<>();

    //存放BeanPostProcess
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    //构造方法中进行初始化
    public XmlBeanFactory(String location) throws Exception {

        beanDefinitionReader = new XmlBeanDefinitionReader();
        loadBeanDefinitions(location);
    }

    /**
     * 加载BeanDefinition
     * @param location
     */
    private void loadBeanDefinitions(String location) throws Exception {

        beanDefinitionReader.loadBeanDefinitions(location);//调用reader中的方法
        registerBeanDefinition();//先注册BeanDefinition
        registerBeanPostProcessor();//再注册BeanPostProcessor
    }

    /**
     *注册BeanDefinition到容器中
     */
    private void registerBeanDefinition() {
        for (Map.Entry<String,BeanDefinition> entry:
        beanDefinitionReader.getRegistry().entrySet()){
            String name = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            //把缓存取出来放进bean容器中
            beanDefinitionMap.put(name,beanDefinition);
            beanDefinitionNames.add(name);
        }
    }

    /**
     *注册BeanPostProcessor相关实现类
     */
    private void registerBeanPostProcessor() throws Exception {
        //根据BeanDefinition记录信息，寻找所有实现了BeanPostProcessor相关实现类
        List beans = getBeansForType(BeanPostProcessor.class);
        for (Object bean : beans){
            addBeanPostProcessor((BeanPostProcessor)bean);
        }
    }

    /**
     * 添加到List中去
     * @param
     */
    private void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.add(beanPostProcessor);
    }

    /**
     * 根据类型获取Bean
     * @param type
     * @return
     */
    public List getBeansForType(Class type) throws Exception {
        List beans = new ArrayList();
        for (String beanDefinitionName : beanDefinitionNames){
            if (type.isAssignableFrom(beanDefinitionMap.get(beanDefinitionName).getBeanClass())){
                beans.add(getBean(beanDefinitionName));
            }
        }
        return beans;
    }

    @Override
    public Object getBean(String name) throws Exception {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        if (beanDefinition == null){
            throw new IllegalArgumentException("no this bean with name " + name);
        }
        //如果取出来没有实例化
        Object bean = beanDefinition.getBean();
        if (bean == null){
            //创建实例
            bean = createBean(beanDefinition);
            //进行初始化
            bean = initializeBean(bean,name);
            beanDefinition.setBean(bean);
        }

        return null;
    }

    /**
     * 创建实例
     * @param beanDefinition
     */
    private Object createBean(BeanDefinition beanDefinition) throws Exception {
        //反射进行实例化
        Object bean = beanDefinition.getBeanClass().newInstance();
        //注入属性
        applyPropertyValues(bean,beanDefinition);
        return bean;
    }

    /**
     * 反射注入属性
     * @param bean
     * @param beanDefinition
     */
    private void applyPropertyValues(Object bean, BeanDefinition beanDefinition) throws Exception {
        //如果 bean 实现了 BeanFactoryAware 接口，
        //Spring 容器在实例化bean的过程中，会将 BeanFactory 容器注入到该bean 中
        if (bean instanceof BeanFactoryAware){
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }

        for (PropertyValue propertyValue : beanDefinition.getPropertyValues().getPropertyValueList()){
            Object value = propertyValue.getValue();
            //引用类型属性
            if (value instanceof BeanReference){
                BeanReference beanReference = (BeanReference) value;
                value = getBean(beanReference.getName());
            }

            try {
                //获取响应属性的set方法
                Method declaredMethod = bean.getClass().getDeclaredMethod(
                        "set"+propertyValue.getName().substring(0,1).toUpperCase()
                        +propertyValue.getName().substring(1),value.getClass()
                );
                //私有方法设置可访问
                declaredMethod.setAccessible(true);
                //set方法注入属性
                declaredMethod.invoke(bean,value);
            }catch (NoSuchMethodException e){
                Field declaredField = bean.getClass().getDeclaredField(propertyValue.getName());
                declaredField.setAccessible(true);
                declaredField.set(bean,value);
            }
        }
    }

    /**
     * 初始化bean
     * @param bean
     * @param name
     * @return
     */
    private Object initializeBean(Object bean, String name) throws Exception {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors){
            //调用前置处理
            bean = beanPostProcessor.postProcessBeforeInitialization(bean,name);
        }

        for (BeanPostProcessor beanPostProcessor : beanPostProcessors){
            bean = beanPostProcessor.postProcessAfterInitialization(bean,name);
        }
        return bean;
    }
}
```

**总结来说，流程就是**

先在构造方法中初始化读取配置文件的`XmlBeanDefinitionReader`的类，调用`loadBeanDefinitions(location);`去加载读出来的<id,BeanDefinition>registry容器。

> //将 <id, BeanDefinition> 键值对缓存在 Map 中
>
> private Map<String, BeanDefinition> registry;

然后在这个方法中先后进行BeanDefinition的注册和BeanPostProcessor的注册。

- BeanDefinition的注册：遍历map，从缓存的registry中取出来以<name,beanDefinition>放入容器`beanDefinitionMap`中去

- 注册BeanPostProcessor相关实现类：

  - `getBeansForType(BeanPostProcessor.class);`方法去获得所有这个类型的Bean

    - 如果是这个类型`beans.add(getBean(beanDefinitionName));`调用getBean

      getBean中如果没有，要进行实例化，这时候BeanPostProcessor类型的就要先实例化

      - ```java
        public Object getBean(String name) throws Exception {
                BeanDefinition beanDefinition = beanDefinitionMap.get(name);
                if (beanDefinition == null){
                    throw new IllegalArgumentException("no this bean with name " + name);
                }
                //如果取出来没有实例化
                Object bean = beanDefinition.getBean();
                if (bean == null){
                    //创建实例
                    bean = createBean(beanDefinition);
                    //进行初始化
                    bean = initializeBean(bean,name);
                    beanDefinition.setBean(bean);
                }
        
                return null;
            }
        ```

        - ```java
          /**
               * 创建实例
               * @param beanDefinition
               */
              private Object createBean(BeanDefinition beanDefinition) throws Exception {
                  //反射进行实例化
                  Object bean = beanDefinition.getBeanClass().newInstance();
                  //注入属性
                  applyPropertyValues(bean,beanDefinition);
                  return bean;
              }
          ```

          - `applyPropertyValues`注入属性，如果 bean 实现了 BeanFactoryAware 接口，会将BeanFactory 容器注入到该bean 中
          - 注入属性基本类型、引用类型都使用反射进行注入属性。先调用set方法，没有set方法就直接用相关字段反射注入

        - 实例化完之后要进行初始化，也就是 在这里做beanPostProcessor处理

          ```java
          private Object initializeBean(Object bean, String name) throws Exception {
                  for (BeanPostProcessor beanPostProcessor : beanPostProcessors){
                      //前置处理
                      bean = beanPostProcessor.postProcessBeforeInitialization(bean,name);
                  }
          
                  for (BeanPostProcessor beanPostProcessor : beanPostProcessors){
                      //后置处理
                      bean = beanPostProcessor.postProcessAfterInitialization(bean,name);
                  }
                  return bean;
              }
          ```

在Spring中，前置处理后置处理中间，还有两步

![](https://img-blog.csdnimg.cn/20200308165401725.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNzYyMDI3,size_16,color_FFFFFF,t_70)

后置处理之后还有几步操作

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200308165435362.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNzYyMDI3,size_16,color_FFFFFF,t_70)

这里直接跳转到使用中

### 2.2 AOP的实现

AOP 是基于动态代理模式实现的，具体实现上可以基于 JDK 动态代理或者 Cglib 动态代理。其中 JDK 动态代理**只能代理实现了接口的对象**，而 Cglib 动态代理则无此限制。所以在为没有实现接口的对象生成代理时，只能使用 Cglib。在项目中，暂时只实现了**基于** **JDK** **动态代理的代理对象生成器**。

关于 AOP 原理这里就不多说了，下面说说 easySpring 中 AOP 的实现步骤。还是像上面一样，先列流程：

1. AOP 逻辑介入 BeanFactory **实例化 bean 的过程**(也就是初始化BeanPostProcessor相关的bean，进行后置处理那块)

2. 根据 Pointcut 定义的匹配规则，判断**当前正在实例化的 bean 是否符合规则**

3. 如果符合，代理生成器将**切面逻辑 Advice** 织入 bean 相关方法中，并**为目标 bean 生成代理对象**

4. 将生成的 bean 的代理对象**返回给 BeanFactory 容器**，到此，AOP 逻辑执行结束

#### 2.2.1 基于 JDK 动态代理的 AOP 实现

具体的工具类较多，这里给出我画的UML图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200308165453498.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNzYyMDI3,size_16,color_FFFFFF,t_70)

本项目中，**代理对象生成器的逻辑**主要写在了 JdkDynamicAopProxy 类中，这个类的有两个方法，其中 getProxy 方法用于生成代理对象。invoke 方法是 InvocationHandler 接口的具体实现，包含了将通知（Advice）织入相关方法中，是2.2节所列流程中第3步流程的具体实现。  

```java
package easySpring.down.aop;

import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 基于 JDK 动态代理的代理对象生成器
 */
public class JdkDynamicAopProxy extends AbstractAopProxy implements InvocationHandler {
    public JdkDynamicAopProxy(AdvisedSupport advised) {
        super(advised);
    }

    /**
     * 为目标bean生成代理对象
     * @return
     */
    public Object getProxy() {
        return Proxy.newProxyInstance(
                getClass().getClassLoader(),
                advised.getTargetSource().getInterfaces(),
                this
        );
    }

    /**
     * InvocationHandler 接口中的 invoke 方法具体实现，封装了具体的代理逻辑
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodMatcher methodMatcher = advised.getMethodMatcher();

        //使用方法匹配器 methodMatcher 测试 bean 中原始方法 method 是否符合匹配规则
        if (methodMatcher != null &&    //传入方法和目标类.Class
        methodMatcher.matchers(method,advised.getTargetSource().getTargetClass())){
            // 获取 Advice。MethodInterceptor 的父接口继承了 Advice
            MethodInterceptor methodInterceptor = advised.getMethodInterceptor();

            //将 bean 的原始 method 封装成 MethodInvocation 实现类对象，
            //将生成的对象传给 Adivce 实现类对象，执行通知逻辑
            return methodInterceptor.invoke(
                    new ReflectiveMethodInvocation(
                            advised.getTargetSource().getTarget(),//对象
                            method,//方法
                            args));//参数
        }else{
            // 当前 method 不符合匹配规则，直接调用 bean 中的原始 method
            return method.invoke(advised.getTargetSource().getTarget(), args);
        }
    }
}
```

 测试方法：

```java
@Test
    public void testGetProxy() throws Exception{
        System.out.println("----------------no proxy:");
        HelloService helloService = new HelloServiceImpl();
        helloService.sayHelloWorld();

        System.out.println("\n------------with proxy:");
        //初始化帮助类
        AdvisedSupport advisedSupport = new AdvisedSupport();
        //设置拦截器
        advisedSupport.setMethodInterceptor(new LogInterceptor());
        //设置目标源类
        TargetSource targetSource = new TargetSource(
                helloService,//目标对象
                HelloServiceImpl.class,//目标对象所属的类
                HelloServiceImpl.class.getInterfaces()//目标对象的接口数组
        );

        advisedSupport.setTargetSource(targetSource);
        //默认所有方法都匹配
        advisedSupport.setMethodMatcher((Method method,Class beanClass)-> true);

        //获得代理对象，动态创建
        helloService = (HelloService) new JdkDynamicAopProxy(advisedSupport).getProxy();
        helloService.sayHelloWorld();

    }
```

下面用个流程图对通知**织入逻辑**进行总结： 

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200308165515672.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNzYyMDI3,size_16,color_FFFFFF,t_70)

### 2.3 AOP 与 IOC 协作

剩下的工作就是2.2节1、2、4所描述的

1. **AOP 逻辑介入 BeanFactory 实例化 bean 的过程**

2. 根据 Pointcut 定义的匹配规则，判断当前正在实例化的 bean 是否符合规则

3. **如果符合，代理生成器将切面逻辑 Advice 织入 bean 相关方法中，并为目标 bean 生成代理对象**

4. **将生成的 bean 的代理对象返回给 BeanFactory 容器，到此，AOP 逻辑执行结束**

在项目中简单集成了 AspectJ。通过集成 AspectJ，使得 toy-spring AOP 可以基于 AspectJ 表达式完成复杂的匹配逻辑。接下来就让我们看看袖珍版 Spring AOP 是怎样实现的吧  

AOP 和 IOC 产生联系的具体实现类是 AspectJAwareAdvisorAutoProxyCreator（下面简称 AutoProxyCreator），这个类实现了 BeanPostProcessor 和 BeanFactoryAware 接口。

> 在Spring中，AbstractAutoProxyCreator 实现了 BeanPostProcessor 接口，这样 AbstractAutoProxyCreator 可以在 bean 初始化时做一些事情。光继承这个接口还不够，**继承这个接口只能获取** bean，要想让 **AOP **生效，还需要拿到切面对象（包含 **Pointcut** **和** **Advice）才行。**
>
> **所以 AbstractAutoProxyCreator 同时继承了 BeanFactoryAware 接口，通过实现该接口，AbstractAutoProxyCreator 子类就可拿到 BeanFactory，有了 BeanFactory，就可以获取 BeanFactory 中**所有的切面对象了。有了目标对象 bean，所有的切面类，此时就可以为 bean 生成代理对象了。
>
> ![在这里插入图片描述](https://img-blog.csdnimg.cn/20200308165555150.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNzYyMDI3,size_16,color_FFFFFF,t_70)

本项目中：

BeanFactory 在注册 BeanPostProcessor 接口相关实现类的阶段，会将其本身注入到 AutoProxyCreator 中，为后面 AOP 给 bean 生成代理对象做准备。**BeanFactory 初始化结束后，AOP** 与 **IOC** 桥梁类 AutoProxyCreator 也完成了实例化（因为她实现了**BeanPostProcessor）**，并被缓存在 BeanFactory 中，静待 BeanFactory 实例化 bean。当外部产生调用，BeanFactory 开始实例化 bean 时。AutoProxyCreator 就开始悄悄的工作了，工作细节如下：

1. 从 BeanFactory 查找实现了 PointcutAdvisor 接口的**切面对象**，切面对象中包含了实现 Pointcut 和 Advice 接口的对象。

2. 使用 Pointcut 中的表达式对象匹配当前 bean 对象。如果匹配成功，进行下一步。否则终止逻辑，返回 bean。
3.  JdkDynamicAopProxy 对象**为匹配到的** **bean** **生成代理对象，并将代理对象返回给** **BeanFactory**。

```java
package easySpring.down.aop;

import easySpring.down.ioc.BeanPostProcessor;
import easySpring.down.ioc.factory.BeanFactory;
import easySpring.down.ioc.factory.BeanFactoryAware;
import easySpring.down.ioc.xml.XmlBeanFactory;
import org.aopalliance.intercept.MethodInterceptor;

import java.util.List;


/**
 * AOP 与 IOC 桥梁类
 */
public class AspectJAwareAdvisorAutoProxyCreator implements BeanPostProcessor , BeanFactoryAware {

    //Bean工厂
    private XmlBeanFactory xmlBeanFactory;

    //不进行前置处理
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }


    /**
     * //使用基于 AspectJ 表达式及其他配置来实现切面功能
     * @param bean
     * @param beanName
     * @return
     * @throws Exception
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {

        //如果我被调用的实例是切面或者增强器，直接返回
        if (bean instanceof AspectJExpressionPointcutAdvisor){
            return bean;
        }
        if (bean instanceof MethodInterceptor){
            return bean;
        }

        // 1.从 BeanFactory 查找 AspectJExpressionPointcutAdvisor 类型的对象
        List<AspectJExpressionPointcutAdvisor> advisors =
                xmlBeanFactory.getBeansForType(AspectJExpressionPointcutAdvisor.class);
        for (AspectJExpressionPointcutAdvisor advisor : advisors){

            //2. 使用 Pointcut 对象匹配当前 bean 对象
            if (advisor.getPointcut().getClassFilter().matchers(bean.getClass())){
                ProxyFactory advisedSupport = new ProxyFactory();
                advisedSupport.setMethodInterceptor((MethodInterceptor) advisor.getAdvice());
                advisedSupport.setMethodMatcher(advisor.getPointcut().getMethodMatcher());

                TargetSource targetSource = new TargetSource(bean,bean.getClass(),
                        bean.getClass().getInterfaces());
                advisedSupport.setTargetSource(targetSource);

                //3. 生成动态代理对象，返回
                return advisedSupport.getProxy();
            }
        }

        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws Exception {
        //可以利用该bean动态获取被spring工厂加载的bean
        xmlBeanFactory = (XmlBeanFactory) beanFactory;
    }
}
```

ProxyFactory 实现代码： 

```java
/**
 * 代理工厂类
 */
public class ProxyFactory extends AdvisedSupport implements AopProxy{

    @Override
    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    private AopProxy createAopProxy() {
        return new JdkDynamicAopProxy(this);//还是调用的之前的
    }
}
```

测试类：

```java
public class XmlBeanFactoryTest {
    @Test
    public void getBean() throws Exception {
        System.out.println("--------- AOP test ----------");
        String location = getClass().getClassLoader().getResource("spring.xml").getFile();
        XmlBeanFactory bf = new XmlBeanFactory(location);
        HelloService helloService = (HelloService) bf.getBean("helloService");
        helloService.sayHelloWorld();
    }
}
```

配置文件spring.xml：

```xml
<bean id="helloService"
          class="easySpring.up.aop.HelloServiceImpl"/>

    <bean id="logInterceptor" class="easySpring.down.aop.LogInterceptor"/>

    <bean id="autoProxyCreator" class="easySpring.down.aop.AspectJAwareAdvisorAutoProxyCreator"/>

	<!-- 切面对象，表达式代表切点，advice代表增强-->
    <bean id="helloServiceAspect"  class="easySpring.down.aop.AspectJExpressionPointcutAdvisor">
        <property name="advice" ref="logInterceptor"/>
        <property name="expression" value="execution(* easySpring.up.aop.HelloService.*(..))"/>
    </bean>
```

