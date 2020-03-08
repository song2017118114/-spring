package easySpring.down.aop;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * 反射方法调用器
 */
public class ReflectiveMethodInvocation implements MethodInvocation {

    protected Object target;//对象

    protected Method method;//方法

    protected Object[] arguments;//参数

    public ReflectiveMethodInvocation(Object target, Method method, Object[] arguments) {
        this.target = target;
        this.method = method;
        this.arguments = arguments;
    }

    //调用
    public Object proceed() throws Throwable {
        return method.invoke(target,arguments);
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Object getThis() {
        return target;
    }

    public AccessibleObject getStaticPart() {
        return method;
    }
}
