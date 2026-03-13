package com.ajaxjs.util.httpremote.call.annotation;

import com.ajaxjs.util.httpremote.call.HttpApiConfig;
import com.ajaxjs.util.httpremote.call.NoConfig;
import com.ajaxjs.util.httpremote.call.NoOp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Url {
    String value();

    Class<? extends Consumer<HttpURLConnection>> initConnection() default NoOp.class;

    Class<? extends HttpApiConfig> config() default NoConfig.class;
}
