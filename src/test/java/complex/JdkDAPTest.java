package complex;

import easySpring.down.aop.AdvisedSupport;
import easySpring.down.aop.JdkDynamicAopProxy;
import easySpring.down.aop.LogInterceptor;
import easySpring.down.aop.TargetSource;
import easySpring.up.aop.HelloService;
import easySpring.up.aop.HelloServiceImpl;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * JDK动态代理测试类
 */
public class JdkDAPTest {

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


}
