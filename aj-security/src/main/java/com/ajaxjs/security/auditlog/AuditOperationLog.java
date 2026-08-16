package com.ajaxjs.security.auditlog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 安全审计操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditOperationLog {
    /**
     * 使用SpEL记录操作日志的详细信息
     *
     * @return 日志模板
     */
    String expression() default "";
}
