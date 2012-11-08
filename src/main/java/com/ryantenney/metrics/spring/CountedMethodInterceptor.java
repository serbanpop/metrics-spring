package com.ryantenney.metrics.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.yammer.metrics.annotation.Counted;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

class CountedMethodInterceptor implements MethodInterceptor, MethodCallback, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CountedMethodInterceptor.class);
    
    private static final MethodFilter filter = new AnnotationFilter(Counted.class);
    
    private final MetricsRegistry metrics;
    private final Class<?> targetClass;
    private final Map<String, Counter> counters;
    private final String scope;

    public CountedMethodInterceptor(final MetricsRegistry metrics, final Class<?> targetClass, final String scope) {
        this.metrics = metrics;
        this.targetClass = targetClass;
        this.counters = new HashMap<String, Counter>();
        this.scope = scope;

        if (log.isDebugEnabled()) {
            log.debug("Creating method interceptor for class {}", targetClass.getCanonicalName());
            log.debug("Scanning for @Counted annotated methods");
        }

        ReflectionUtils.doWithMethods(targetClass, this, filter);
    }
 
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Counter counter = counters.get(invocation.getMethod().getName());
        if (counter != null) {
            counter.inc();
        }
        try {
            return invocation.proceed();
        } finally {
            if (counter != null) {
                counter.dec();
            }
        }
    }
    
    @Override
    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        final Counted annotation = method.getAnnotation(Counted.class);
        final MetricName metricName = Util.forCountedMethod(targetClass, method, annotation, scope);
        final Counter counter = metrics.newCounter(metricName);
        
        counters.put(method.getName(), counter);

        if (log.isDebugEnabled()) {
            log.debug("Created counter {} for method {}", metricName, method.getName());
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
    
}
