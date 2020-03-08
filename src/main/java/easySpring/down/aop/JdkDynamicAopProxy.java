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
