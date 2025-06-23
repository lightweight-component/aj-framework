package com.ajaxjs.tracing.model;

/**
 * Api请求阶段枚举类
 *
 * @author Emily
 * @since Created in 2023/4/22 3:53 PM
 */
public enum TracingStage {
    //参数校验之前
    PARAMETER,
    //控制器方法调用之前
    CONTROLLER,
    OTHER;
}
