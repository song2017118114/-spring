package easySpring.down.aop;

/**
 * 类过滤器
 */
public interface ClassFilter {

    Boolean matchers(Class beanClass) throws Exception;
}
