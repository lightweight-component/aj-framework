package com.ajaxjs.security.limit.leakbucket;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the leaky bucket limit component.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LeakyBucketLimit {
    /**
     * 限流器名称
     *
     * @return the Spring bean name of the limiter
     */
    String limitBeanName();

    /**
     * 拦截器class
     *
     * @return the limiter implementation class
     */
    Class<?> limitClass() default LeakyBucket.class;
}
