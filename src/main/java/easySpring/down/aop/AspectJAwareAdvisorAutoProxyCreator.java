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

        //如果已经被增强过了，直接返回
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
