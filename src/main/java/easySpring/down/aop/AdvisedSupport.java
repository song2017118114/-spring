package easySpring.down.aop;
import org.aopalliance.intercept.MethodInterceptor;


public class AdvisedSupport {

    private TargetSource targetSource;//目标源

    private MethodInterceptor methodInterceptor;//方法拦截器

    private MethodMatcher methodMatcher;//方法匹配器

    public TargetSource getTargetSource() {
        return targetSource;
    }

    public void setTargetSource(TargetSource targetSource) {
        this.targetSource = targetSource;
    }

    public MethodInterceptor getMethodInterceptor() {
        return methodInterceptor;
    }

    public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
    }

    public MethodMatcher getMethodMatcher() {
        return methodMatcher;
    }

    public void setMethodMatcher(MethodMatcher methodMatcher) {
        this.methodMatcher = methodMatcher;
    }
}
