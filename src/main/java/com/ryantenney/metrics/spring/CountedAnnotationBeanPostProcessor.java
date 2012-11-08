package com.ryantenney.metrics.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import com.yammer.metrics.annotation.Counted;
import com.yammer.metrics.core.MetricsRegistry;

public class CountedAnnotationBeanPostProcessor extends AbstractProxyingBeanPostProcessor {

    private static final long serialVersionUID = -7586270312676240073L;
    
    private final Pointcut pointcut = new AnnotationMatchingPointcut(null, Counted.class);
    private final MetricsRegistry metrics;
    private final String scope;
    
    public CountedAnnotationBeanPostProcessor(final MetricsRegistry metrics, final ProxyConfig config, final String scope) {
        this.metrics = metrics;
        this.scope = scope;
        
        this.copyFrom(config);
    }
    
    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }
    
    @Override
    public MethodInterceptor getMethodInterceptor(Class<?> targetClass) {
        return new CountedMethodInterceptor(metrics, targetClass, scope);
    }

}
