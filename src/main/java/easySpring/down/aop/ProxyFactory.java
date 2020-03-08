package easySpring.down.aop;

/**
 * 代理工厂类
 */
public class ProxyFactory extends AdvisedSupport implements AopProxy{

    @Override
    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    private AopProxy createAopProxy() {
        return new JdkDynamicAopProxy(this);
    }
}
