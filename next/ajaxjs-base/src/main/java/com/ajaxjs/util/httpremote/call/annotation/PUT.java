package com.ajaxjs.util.httpremote.call.annotation;

import com.ajaxjs.util.httpremote.call.NoOp;
import com.ajaxjs.util.httpremote.model.PayloadType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PUT {
    /**
     * The URL to call.
     *
     * @return The URL to call.
     */
    String value() default "";

    PayloadType type() default PayloadType.JSON_BODY;

    /**
     * How to initialize the connection?
     *
     * @return The callback class to initialize the connection.
     */
    Class<? extends Consumer<HttpURLConnection>> initConnection() default NoOp.class;
}
