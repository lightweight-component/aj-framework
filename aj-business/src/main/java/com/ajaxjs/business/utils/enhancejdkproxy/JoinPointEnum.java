package com.ajaxjs.business.utils.enhancejdkproxy;

/**
 * 连接点枚举
 **/
public enum JoinPointEnum {
    BEFORE("before", "方法执行前增强"),
    AFTER_RETURN("after_return", "方法正常返回后增强"),
    AFTER_THROW("after_throw", "方法异常时增强"),
    AFTER("after", "方法执行结束增强，无论正常执行还是抛出异常"),
    AROUND("around", "方法环绕增强");

    /**
     * 连接点名称
     */
    private final String name;

    /**
     * 连接点描述
     */
    private final String desc;

    JoinPointEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
