package com.ajaxjs.security.nonrepeatsubmit;

import java.lang.annotation.*;

/**
 * 防重复提交注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface NonRepeatSubmit {
    enum Type {
        /**
         * 通过 Token 验证，时候没有 userId 的场景（例如注册）
         */
        TOKEN,

        /**
         * 根据 userId、method、params 等自动生成唯一 id
         */
        AUTO
    }

    /**
     * 验证类型
     *
     * @return 验证类型
     */
    Type type() default Type.AUTO;

    /**
     * 存活时间 单位：秒
     *
     * @return 时间
     */
    int ttl() default 60;
}
