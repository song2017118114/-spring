package simple;

import org.junit.Test;
import easySpring.up.aop.*;

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
