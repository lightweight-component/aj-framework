package com.ajaxjs.util.http_request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A HTTP Request
 */
@Target(ElementType.TYPE)
// Specify that this annotation should be retained at runtime so it can be read reflectively
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpRequest {
    String url() default "";
}
