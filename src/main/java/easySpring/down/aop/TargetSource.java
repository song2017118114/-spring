package easySpring.down.aop;

public class TargetSource {

    private Object target;//目标对象

    private Class<?> targetClass;//目标对象所属的类

    private Class<?>[] interfaces;//目标对象的接口数组

    public TargetSource(Object target, Class<?> targetClass, Class<?>... interfaces) {
        this.target = target;
        this.targetClass = targetClass;
        this.interfaces = interfaces;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Object getTarget() {
        return target;
    }

    public Class<?>[] getInterfaces() {
        return interfaces;
    }
}
