package com.ajaxjs.security.limit.leakbucket;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LeakyBucketLimit {
    /**
     * 限流器名称
     */
    String limitBeanName();

    /**
     * 拦截器class
     */
    Class<?> limitClass() default LeakyBucket.class;
}
