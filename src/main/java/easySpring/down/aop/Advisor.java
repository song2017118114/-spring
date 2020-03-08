package easySpring.down.aop;
import org.aopalliance.aop.Advice;

/**
 * 增强接口
 */
public interface Advisor {

    Advice getAdvice();
}
