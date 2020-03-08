package easySpring.down.aop;

/**
 * 切面接口
 */
public interface PointcutAdvisor extends Advisor {

    Pointcut getPointcut();

}
