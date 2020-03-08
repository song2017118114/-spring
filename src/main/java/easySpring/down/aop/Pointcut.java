package easySpring.down.aop;

/**
 * 切入点接口
 */
public interface Pointcut {

    ClassFilter getClassFilter();

    MethodMatcher getMethodMatcher();

}
