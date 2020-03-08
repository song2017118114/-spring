package easySpring.down.aop;

import org.aopalliance.aop.Advice;


/**
 * 切面对象
 */
public class AspectJExpressionPointcutAdvisor implements PointcutAdvisor {

    //切面引入"切点"，实现"增强器"接口
    private AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();

    private Advice advice;//advice增强

    @Override
    public Advice getAdvice() {
        return advice;
    }

    public void setExpression(String expression) {
        pointcut.setExpression(expression);
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

}
