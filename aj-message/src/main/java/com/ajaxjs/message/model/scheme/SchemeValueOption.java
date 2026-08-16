package com.ajaxjs.message.model.scheme;

import java.lang.annotation.*;
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SchemeValueOption {
    String key();

    String label();
}
