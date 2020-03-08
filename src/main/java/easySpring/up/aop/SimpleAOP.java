package easySpring.up.aop;

import java.lang.reflect.Proxy;

public class SimpleAOP {

    public static Object getProxyInstance(Object bean,Advice advice){
        return Proxy.newProxyInstance(
                SimpleAOP.class.getClassLoader(),//用哪个类加载器去加载代理对象
                bean.getClass().getInterfaces(),//动态代理类需要实现的接口
                advice);//传入的InvocationHandler，用这个来增强
    }
}
