package easySpring.down.ioc.factory;

/**
 *让Bean在容器中有存在感，为了得到BeanFactory的引用，进而得到想要的Bean
 */
public interface BeanFactoryAware {

    //可以利用该bean动态获取被spring工厂加载的bean

    void setBeanFactory(BeanFactory beanFactory) throws Exception;

}
