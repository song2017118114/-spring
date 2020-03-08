package easySpring.down.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 测试用的，一个拦截器
 */
public class LogInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        System.out.println(methodInvocation.getMethod().getName()+"method start");
        //执行方法
        Object obj = methodInvocation.proceed();
        System.out.println(methodInvocation.getMethod().getName()+"method end");
        return obj;
    }
}
