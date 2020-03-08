package easySpring.down.ioc.xml;

import easySpring.down.ioc.BeanDefinition;
import easySpring.down.ioc.BeanPostProcessor;
import easySpring.down.ioc.BeanReference;
import easySpring.down.ioc.PropertyValue;
import easySpring.down.ioc.factory.BeanFactory;
import easySpring.down.ioc.factory.BeanFactoryAware;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        registerBeanDefinition();
        registerBeanPostProcessor();
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
            //前置处理
            bean = beanPostProcessor.postProcessBeforeInitialization(bean,name);
        }

        for (BeanPostProcessor beanPostProcessor : beanPostProcessors){
            bean = beanPostProcessor.postProcessAfterInitialization(bean,name);
        }
        return bean;
    }
}
