package com.ajaxjs.security.antireptile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要反爬检查的 Spring MVC 控制器方法。
 * <p>注解在运行期由 {@link AntiReptileInterceptor} 读取。即使未启用全局过滤，
 * 被标记的方法也会进入规则检查；组件本身仍需通过配置启用。该注解仅用于方法。</p>
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface AntiReptile {
}
