package com.ajaxjs.framework.entity;

import com.ajaxjs.framework.BusinessException;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 禁止写操作，for 演示用
 */
public class DisableWritePlugins {
    /**
     * 可以设置 createDate, createBy 等字段
     */
    public static final Consumer<Map<String, Object>> beforeCreate = params -> {
        throw new BusinessException("当前禁止写操作");
    };

    /**
     * 可以设置 updateDate, updateBy 等字段
     */
    public static final Consumer<Map<String, Object>> beforeUpdate = params -> {
        throw new BusinessException("当前禁止写操作");
    };

    /**
     * 更新之前的执行的回调函数，可以设置 updateBy 等的字段
     * isHasIsDeleted = 根据是否有删除标记字段来构造不同的 SQL 语句
     * 这里只能拼凑 SQL 字符串了
     */
    public static final BiFunction<Boolean, String, String> beforeDelete = (Boolean isHasIsDeleted, String sql) -> {
        throw new BusinessException("当前禁止写操作");
    };
}
