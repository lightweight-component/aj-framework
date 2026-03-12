package com.ajaxjs.util.log;

import com.ajaxjs.util.CommonConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Log this operation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EnableOperationLog {
    /**
     * The title of the log
     *
     * @return The title of the log
     */
    String value() default CommonConstant.EMPTY_STRING;
}
