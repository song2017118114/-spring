

- [0.1. 简单的AOP实现：](#01-简单的aop实现)
- [1. IOC的实现](#1-ioc的实现)
    - [1.0.1. BeanFactory的生命流程](#101-beanfactory的生命流程)
    - [1.0.2. BeanDefinition及其他一些类的介绍](#102-beandefinition及其他一些类的介绍)
    - [1.0.3. xml的解析](#103-xml的解析)
    - [1.0.4. 注册BeanPostProcessor](#104-注册beanpostprocessor)
    - [1.0.5. getBean过程解析](#105-getbean过程解析)
- [2. AOP实现](#2-aop实现)
    - [2.0.6. 基于 JDK 动态代理的 AOP 实现](#206-基于-jdk-动态代理的-aop-实现)



最简单的IOC容器只需要4步即可实现

1. 加载xml配置文件，遍历其中的标签
2. 获取标签中的id和class属性，加载class属性对应的类，并创建bean
3. 遍历标签中的标签，获取属性值，并将属性值填充到bean中
4. 将bean注册到bean容器中

**SimpleIOC**：

```java
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

    public Object getBean(String name){
        Object bean = beanMap.get(name);
        if (bean == null){
            throw new IllegalArgumentException("没有叫"+name+"的bean");
        }
        return bean;
    }

    private void loadBeans(String location) throws Exception{
        //加载xml配置文件
        InputStream inputStream = new FileInputStream(location);
        //工厂模式
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputStream);
        Element root = doc.getDocumentElement();
        NodeList nodes = root.getChildNodes();

        //遍历<bean>标签
        for (int i = 0;i < nodes.getLength();i++){
            Node node = nodes.item(i);
            if (node instanceof Element){
                Element ele = (Element) node;
                String id = ele.getAttribute("id");
                String className = ele.getAttribute("class");

                //加载beanClass
                Class beanClass = null;
                try{//反射
                    beanClass = Class.forName(className);
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                    return;
                }

                //创建bean
                Object bean = beanClass.newInstance();

                //遍历<property>标签
                NodeList propertyNodes = ele.getElementsByTagName("property");
                for (int j = 0;j <propertyNodes.getLength();j++){
                    Node propertyNode = propertyNodes.item(j);
                    if (propertyNode instanceof Element){
                        Element propertyElement = (Element)propertyNode;
                        String name = propertyElement.getAttribute("name");
                        String value = propertyElement.getAttribute("value");

                        //利用反射将bean相关字段访问权限设为可访问
                        Field declaredField = bean.getClass().getDeclaredField(name);
                        declaredField.setAccessible(true);

                        if (value != null && value.length() > 0){
                            //将属性值填到相关字段中
                            declaredField.set(bean,value);
                        }else {
                            String ref = propertyElement.getAttribute("ref");
                            if (ref == null || ref.length() == 0){
                                throw new IllegalArgumentException("ref 参数错误");
                            }

                            //将引用填充到相关字段中
                            declaredField.set(bean,getBean(ref));
                        }

                        //将bean注册到bean容器中
                        registerBean(id,bean);
                    }
                }
            }
        }
    }

    private void registerBean(String id,Object bean){
        
        beanMap.put(id,bean);
    }
}
```

**Car:**

```java
package com.sc.domain;

import java.io.Serializable;

public class Car implements Serializable {

    private String name;
    private String length;
    private String width;
    private String height;
    private Wheel wheel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public Wheel getWheel() {
        return wheel;
    }

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    @Override
    public String toString() {
        return "Car{" +
                "name='" + name + '\'' +
                ", length='" + length + '\'' +
                ", width='" + width + '\'' +
                ", height='" + height + '\'' +
                ", wheel=" + wheel +
                '}';
    }
}
```

**Wheel:**

```java
package com.sc.domain;

public class Wheel {
    private String brand;
    private String specification;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    @Override
    public String toString() {
        return "Wheel{" +
                "brand='" + brand + '\'' +
                ", specification='" + specification + '\'' +
                '}';
    }
}
```

**ioc.xml**

```xml
<beans>
    <bean id="wheel" class="com.sc.domain.Wheel">
        <property name="brand" value="Michelin"></property>
        <property name="specification" value="265/60 R18"/>
    </bean>

    <bean id="car" class="com.sc.domain.Car">
        <property name="name" value="benchi G 500"/>
        <property name="length" value="4717mm"/>
        <property name="width" value="1949mm"/>
        <property name="height" value="1855mm"/>
        <property name="wheel" ref="wheel"/>
    </bean>
</beans>
```

**main:**

```java
public class SimpleIOCTest {

    public static void main(String args[]) throws Exception {
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

**结果：**

```java
Wheel{brand='Michelin', specification='265/60 R18'}
Car{name='benchi G 500', length='4717mm', width='1949mm', height='1855mm', wheel=Wheel{brand='Michelin', specification='265/60 R18'}}
```

## 0.1. 简单的AOP实现：

**五种类型的通知**

- Before：在目标法执行前执行通知
- After：在目标法执行后执行通知，不关系返回值是什么
- After-returning：在目标方法执行后，执行通知
- After-throwing：目标方法抛出异常后执行通知
- Around：目标法被通知包裹，通知在目标方法执行前后都会被调用

**切点**：在何处通知，通过匹配规则找到连接点，AOP会在这些连接点上织入通知

**切面**：切面包含了 通知 和 切点 ，在何时何处执行切面逻辑

此处我们通过动态代理实现AOP，步骤如下：

1. 定义一个包含切面逻辑的对象**logMethodInvocation**
2. 定义一个Advice对象（实现了InvocationHandler接口），并将上面的**logMethodInvocation**和目标对象传入
3. 将上面的Advice对象和目标对象传给JDK动态代理方法，为目标对象生成代理

**MethodInvocation:**

```java
/**
 * 他的实现类包含切面逻辑
 */
public interface MethodInvocation {
    void invoke();
}
```

**Advice:**

```java
/**
 * 继承了InvocationHandler接口
 * 每个代理类的调用处理程序都必须实现InvocationHandler接口
 * 相当于所有通知类的父类
 */
public interface Advice extends InvocationHandler {
}
```

**BeforAdvice:**

```java
/**
 * 实现了Advice接口，是一个前置通知
 */
public class BeforeAdvice implements Advice {

    //代理类中的真实对象
    private Object bean;
    //要实现的逻辑
    private MethodInvocation methodInvocation;

    public BeforeAdvice(Object bean,MethodInvocation methodInvocation){
        this.bean = bean;
        this.methodInvocation = methodInvocation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //在目标方法执行前 调用通知
        methodInvocation.invoke();
        //目标法执行
        return method.invoke(bean,args);
    }
}
```

**SimpleAOP：**

```java
import java.lang.reflect.Proxy;
/**
 * 生成代理类
 */
public class SimpleAOP {

    public static Object getProxy(Object bean,Advice advice){
        return Proxy.newProxyInstance(SimpleAOP.class.getClassLoader(),
                bean.getClass().getInterfaces(),advice);
    }
}
```

**HelloService接口：**

```java
/**
 * 目标对象接口
 */
public interface HelloService {
    void sayHelloWorld();
}
```

**HelloServiceImpl:**

```java
/**
 * 接口实现类
 */
public class HelloServiceImpl implements HelloService{
    @Override
    public void sayHelloWorld() {
        System.out.println("hello world aop!");
    }
}
```

**SimpleAOPTest:**

```java
/**
 * aop测试类
 */
public class SimpleAOPTest {

    public static void main(String[] args) {
        //1.创建一个MethodInvocation实现类
        MethodInvocation logTask = ()-> System.out.println("log task start");
        //目标对象
        HelloService helloServiceImpl = new HelloServiceImpl();
        //2.创建一个Advice
        Advice beforeAdvice = new BeforeAdvice(helloServiceImpl,logTask);
        //3.为目标对象生成代理
        HelloService helloServiceImplProxy = (HelloService)
                SimpleAOP.getProxy(helloServiceImpl,beforeAdvice);

        helloServiceImplProxy.sayHelloWorld();
    }
}
```

最后的输出：

```java
log task start
hello world aop!
```

---

# 1. IOC的实现

### 1.0.1. BeanFactory的生命流程

1. BeanFactory加载Bean配置文件，将读到的Bean配置**封装成BeanDefinition**对象
2. 将封装好的BeanDefinition对象注册到**BeanDefinition容器**中
3. 注册BeanPostProcessor相关实现类到**BeanPostProcessor容器**中
4. BeanFactory进入**就绪**状态
5. 外部调用BeanFactory的**getBean(String name)**方法，BeanFactory着手**实例化相应的bean**
6. 重复3、4，直至程序退出，BeanFactory被销毁

### 1.0.2. BeanDefinition及其他一些类的介绍

- **BeanDefinition**：作用是根据Bean的配置信息生产成本相应的Bean详情对象，如果把Bean比喻为电脑 ，那么BeanDefinition就是电脑的**配置清单**，可以透过电脑看到电脑的详细配置。

- **BeanReference**：保存bean配置中ref属性对应的值，在后续BeanFactory实例化bean时，会根据BeanReference保存的值去实例化bean所依赖的其他bean。

- **PropertyValues**和**PropertyValue**

  - PropertyValue：name、value字段记录bean标签中的属性值

  - PropertyValues：虽然只是上面的复数形式，也就是相当于一个属性的List集合，那为什么不直接使用List而要定义一个新的类呢？——为了获得一定的控制权

    比如：定义这个PropertyValues类把对属性的列表**加入操作**封装起来，内部可以添加一些其他的处理，之后再调用List的add方法

### 1.0.3. xml的解析

BeanFactory初始化时，会根据传入的xml配置文件路径来加载配置文件，然而BeanFactory值需要管理容器中的bean就可以了，加载和解析配置文件的任务由BeanDefinitionReader的实现类**XmlBeanDefinitionReader**去做就可以了

1. 将xml配置文件加载到内存中
2. 获取根标签下的所有标签
3. 遍历获取到的标签列表，取出id、class属性
4. 创建BeanDefinition对象，并将刚刚读取到的id、class属性保存到对象中
5. 遍历标签下的标签，读取属性值并保存到对象中
6. 将<id,BeanDefinition>键值对缓存在Map中，留在后面使用
7. 重复3、4、5、6步，直至解析结束

### 1.0.4. 注册BeanPostProcessor

BeanPostProcessor是Spring对外拓展的接口之一，主要用途：提供一个机会，让开发人员能够插手bean的实例化过程。我们可以在bean实例化的过程中对bean进行一些处理，比如AOP织入相关bean中。

康康BeanFactory如何注册BeanPostProcessor相关实现类的

XmlBeanDefinitionReader在完成解析工作后，BeanFactory会将键值对<id,BeanDefinition>注册到自己的**beanDefinitionMap**中。BeanFactory注册好**BeanDefinition**后，就立即开始注册BeanPostProcessor的相关实现类。这个过程

1. 根据BeanDefinition记录的信息，寻找所有实现了BeanPostProcessor的接口的类
2. 实例化BeanPostProcessor接口的实现类
3. 将实例化好的对象放入**List**中
4. 重复2、3步，直至所有的实现类完成注册

### 1.0.5. getBean过程解析

在完成xml的解析，BeanDefinition的注册，以及BeanPostProcessor的注册后，BeanFactory的初始化工作就算是结束了，此时BeanFactory处于就绪状态，等待外部程序的调用。它具有延迟实例化bean的特性，也就是等外部程序需要的时候，才实例化。这样做

- 提高BeanFactory的初始化速度
- 节省内存资源

看看Spring bean实例化过程 ：

 ![bean实例化过程](https://blog-pictures.oss-cn-shanghai.aliyuncs.com/bean%e5%ae%9e%e4%be%8b%e5%8c%96%e8%bf%87%e7%a8%8b.png) 

我们仿写的过程中简化流程：

1. 实例化bean对象
2. 设置对象属性： 将配置文件中配置的属性填充到刚刚创建的 bean 对象中 
3. 检查Aware相关接口并设置相关依赖
4. BeanPostProcessor前置处理 postProcessBeforeInitialization(Object bean, String beanName) 
5. BeanPostProcessor后置处理 postProcessAfterInitialization(Object bean, String beanName) 
6. 使用中

先看相关的接口：

BeanFactory接口：

```java
/**
 * 工厂接口
 */
public interface BeanFactory {
    Object getBean(String beanId) throws Exception;
}
```

BeanFactoryAware接口：设置为别的BeanFactory

```java
public interface BeanFactoryAware {

    void setBeanFactory(BeanFactory beanFactory) throws Exception;
}
```

BeanDefinitionReader接口：把配置文件读到BeanDefinition中去

```java
public interface BeanDefinitionReader {

    void loadBeanDefinitions(String location) throws FileNotFoundException, Exception;

}
```

BeanPostProcessor 插手实例化的接口：

```java
public interface BeanPostProcessor {

    /**
     * 前置处理
     */
    Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception;

    /**
     * 后置处理
     */
    Object postProcessAfterInitialization(Object bean, String beanName) throws Exception;
}
```

BeanDefinition类：提供了相应属性值和get、set方法

```java
public class BeanDefinition {

    private Object bean;

    private Class beanClass;

    private String beanClassName;

    private PropertyValues propertyValues = new PropertyValues();

    public BeanDefinition() {
    }

    public void setBean(Object bean) {
        this.bean = bean;
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

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
        try {
            this.beanClass = Class.forName(beanClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Object getBean() {
        return bean;
    }

    public PropertyValues getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }
}
```

BeanReference类：bean的引用

```java
public class BeanReference {

    private String name;

    private Object bean;

    public BeanReference(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
}
```

PropertyValue、PropertyValues：属性值

```java
public class PropertyValue {

    private final String name;

    private final Object value;

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
```

```java
public class PropertyValues {

    private final List<PropertyValue> propertyValueList = new ArrayList<PropertyValue>();

    public void addPropertyValue(PropertyValue pv) {
        // 在这里可以对参数值 pv 做一些处理，如果直接使用 List，则就不行了
        this.propertyValueList.add(pv);
    }

    public List<PropertyValue> getPropertyValues() {
        return this.propertyValueList;
    }

}
```

XmlBeanDefinitionReader： 解析 xml  

```java
public class XmlBeanDefinitionReader implements BeanDefinitionReader {

    private Map<String,BeanDefinition> registry;

    public XmlBeanDefinitionReader() {
        registry = new HashMap<>();//实例化的时候才加载map
    }

    /**
     * 读入配置文件，加载BeanDefinitions
     */
    @Override
    public void loadBeanDefinitions(String location) throws Exception {
        InputStream inputStream = new FileInputStream(location);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputStream);
        Element root = doc.getDocumentElement();
        parseBeanDefinitions(root);
    }

    /**
     * 遍历root结点
     */
    private void parseBeanDefinitions(Element root) throws Exception {
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                parseBeanDefinition(ele);
            }
        }
    }

    /**
     * 遍历bean
     */
    private void parseBeanDefinition(Element ele) throws Exception {
        String name = ele.getAttribute("id");
        String className = ele.getAttribute("class");
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClassName(className);
        processProperty(ele, beanDefinition);
        registry.put(name, beanDefinition);
    }

    /**
     * 遍历属性
     */
    private void processProperty(Element ele, BeanDefinition beanDefinition) {
        NodeList propertyNodes = ele.getElementsByTagName("property");
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Node propertyNode = propertyNodes.item(i);
            if (propertyNode instanceof Element) {
                Element propertyElement = (Element) propertyNode;
                String name = propertyElement.getAttribute("name");
                String value = propertyElement.getAttribute("value");
                if (value != null && value.length() > 0) {
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
                } else {
                    String ref = propertyElement.getAttribute("ref");
                    if (ref == null || ref.length() == 0) {
                        throw new IllegalArgumentException("ref config error");
                    }
                    BeanReference beanReference = new BeanReference(ref);
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, beanReference));
                }
            }
        }
    }

    /**
     * 获取Map
     */
    public Map<String, BeanDefinition> getRegistry() {
        return registry;
    }
}
```

XmlBeanFactory：

```java
public class XmlBeanFactory implements BeanFactory {

    /**
     * 把<id,BeanDefinition>的键值对注册到这个里头
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private List<String> beanDefinitionNames = new ArrayList<>();

    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<BeanPostProcessor>();

    /**
     * 关联过来
     */
    private XmlBeanDefinitionReader beanDefinitionReader;


    public XmlBeanFactory(String location) throws Exception {
        beanDefinitionReader = new XmlBeanDefinitionReader();
        loadBeanDefinitions(location);
    }
    /**
     * 获取bean
     */
    public Object getBean(String name) throws Exception {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        if (beanDefinition == null) {
            throw new IllegalArgumentException("no this bean with name " + name);
        }

        Object bean = beanDefinition.getBean();
        if (bean == null) {
            bean = createBean(beanDefinition);
            bean = initializeBean(bean, name);
            beanDefinition.setBean(bean);
        }

        return bean;
    }
    /**
     * 创建Bean，通过反射
     */
    private Object createBean(BeanDefinition bd) throws Exception {
        Object bean = bd.getBeanClass().newInstance();
        applyPropertyValues(bean, bd);

        return bean;
    }
    /**
     * 注入属性
     */
    private void applyPropertyValues(Object bean, BeanDefinition bd) throws Exception {
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }
        for (PropertyValue propertyValue : bd.getPropertyValues().getPropertyValues()) {
            Object value = propertyValue.getValue();
            if (value instanceof BeanReference) {
                BeanReference beanReference = (BeanReference) value;
                value = getBean(beanReference.getName());
            }

            try {
                Method declaredMethod = bean.getClass().getDeclaredMethod(
                        "set" + propertyValue.getName().substring(0, 1).toUpperCase()
                                + propertyValue.getName().substring(1), value.getClass());
                declaredMethod.setAccessible(true);

                declaredMethod.invoke(bean, value);
            } catch (NoSuchMethodException e) {
                Field declaredField = bean.getClass().getDeclaredField(propertyValue.getName());
                declaredField.setAccessible(true);
                declaredField.set(bean, value);
            }
        }
    }
    /**
     * 初始化
     */
    private Object initializeBean(Object bean, String name) throws Exception {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.postProcessBeforeInitialization(bean, name);
        }

        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.postProcessAfterInitialization(bean, name);
        }

        return bean;
    }

    /**
     * 载入BeanDefinition和BeanPostProcessor
     */
    private void loadBeanDefinitions(String location) throws Exception {
        beanDefinitionReader.loadBeanDefinitions(location);
        registerBeanDefinition();
        registerBeanPostProcessor();
    }

    /**
     * 加入到Map中
     */
    private void registerBeanDefinition() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionReader.getRegistry().entrySet()) {
            String name = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            beanDefinitionMap.put(name, beanDefinition);
            beanDefinitionNames.add(name);
        }
    }

    /**
     * 
     */
    public void registerBeanPostProcessor() throws Exception {
        List beans = getBeansForType(BeanPostProcessor.class);
        for (Object bean : beans) {
            addBeanPostProcessor((BeanPostProcessor) bean);
        }
    }

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.add(beanPostProcessor);
    }

    /**
     * 根据类型获取Bean
     */
    public List getBeansForType(Class type) throws Exception {
        List beans = new ArrayList<>();
        for (String beanDefinitionName : beanDefinitionNames) {
            if (type.isAssignableFrom(beanDefinitionMap.get(beanDefinitionName).getBeanClass())) {
                beans.add(getBean(beanDefinitionName));
            }
        }
        return beans;
    }
}
```

# 2. AOP实现

 AOP 是基于动态代理模式实现的，具体实现上可以基于 JDK 动态代理或者 Cglib 动态代理。其中 JDK 动态代理只能代理实现了接口的对象，而 Cglib 动态代理则无此限制。 

简化实现流程：

1. AOP 逻辑介入 BeanFactory **实例化 bean** 的过程
2. 根据 Pointcut 定义的匹配规则，**判断当前正在实例化的 bean 是否符合规则**
3. 如果符合，代理生成器将切面逻辑 Advice 织入 bean 相关方法中，并为目标 bean 生成**代理对象**
4. 将生成的 bean 的代理对象**返回给 BeanFactory** **容器**，到此，AOP 逻辑执行结束

### 2.0.6. 基于 JDK 动态代理的 AOP 实现

 代理对象生成器的逻辑主要写在了 **JdkDynamicAopProxy** 类中，这个类的有两个方法，其中 **getProxy** 方法用于生成代理对象。**invoke** 方法是 InvocationHandler 接口的具体实现，包含了将通知（Advice）织入相关方法中 

```java
public abstract class AbstractAopProxy implements AopProxy {

    protected AdvisedSupport advised;

    public AbstractAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }
}

/**
 * 基于 JDK 动态代理的代理对象生成器
 * Created by code4wt on 17/8/16.
 */
final public class JdkDynamicAopProxy extends AbstractAopProxy implements InvocationHandler {

    public JdkDynamicAopProxy(AdvisedSupport advised) {
        super(advised);
    }

    /**
     * 为目标 bean 生成代理对象
     * @return bean 的代理对象
     */
    @Override
    public Object getProxy() {
        return Proxy.newProxyInstance(getClass().getClassLoader(), advised.getTargetSource().getInterfaces(), this);
    }

    /**
     * InvocationHandler 接口中的 invoke 方法具体实现，封装了具体的代理逻辑
     * @param proxy
     * @param method
     * @param args
     * @return 代理方法或原方法的返回值
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodMatcher methodMatcher = advised.getMethodMatcher();
        
        // 1. 使用方法匹配器 methodMatcher 测试 bean 中原始方法 method 是否符合匹配规则
        if (methodMatcher != null && methodMatcher.matchers(method, advised.getTargetSource().getTargetClass())) {
            
            // 获取 Advice。MethodInterceptor 的父接口继承了 Advice
            MethodInterceptor methodInterceptor = advised.getMethodInterceptor();
            
           /* 
            * 2. 将 bean 的原始方法 method 封装在 MethodInvocation 接口实现类对象中，
            * 并把生成的对象作为参数传给 Adivce 实现类对象，执行通知逻辑
            */ 
            return methodInterceptor.invoke(
                    new ReflectiveMethodInvocation(advised.getTargetSource().getTarget(), method, args));
        } else {
            // 2. 当前 method 不符合匹配规则，直接调用 bean 的原始方法 method
            return method.invoke(advised.getTargetSource().getTarget(), args);
        }
    }
}
```

 ![img](https://blog-pictures.oss-cn-shanghai.aliyuncs.com/%e9%80%9a%e7%9f%a5%e7%bb%87%e5%85%a5%e6%b5%81%e7%a8%8b%e5%9b%be.png) 

 ProxyFactory 实现代码： 	

```java
/**
 * AopProxy 实现类的工厂类
 */
public class ProxyFactory extends AdvisedSupport implements AopProxy {
    @Override
    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    private AopProxy createAopProxy() {
        return new JdkDynamicAopProxy(this);
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





