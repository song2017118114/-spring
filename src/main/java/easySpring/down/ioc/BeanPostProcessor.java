package easySpring.down.ioc;

/**
 * 提供机会让开发人员能够插手bean的实例化过程
 */
public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(Object bean,String beanName) throws Exception;

    Object postProcessAfterInitialization(Object bean,String beanName) throws Exception;
}
