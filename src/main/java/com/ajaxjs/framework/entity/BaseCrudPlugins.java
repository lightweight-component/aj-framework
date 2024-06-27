package com.ajaxjs.framework.entity;

import com.ajaxjs.data.data_service.DataServiceUtils;
import com.ajaxjs.data.data_service.FastCRUD;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 默认插件
 */
public class BaseCrudPlugins {
    /**
     * 可以设置 createDate, createBy 等字段
     */
    public static final Consumer<Map<String, Object>> beforeCreate = params -> {
        Object user = DataServiceUtils.getCurrentUser();
        long userId = DataServiceUtils.executeMethod(user, "getId", long.class);
        String userName = DataServiceUtils.executeMethod(user, "getName", String.class);

        params.put("creator_id", userId);
        params.put("creator", userName);
        params.put("updater_id", userId);
        params.put("updater", userName);
    };

    /**
     * 可以设置 updateDate, updateBy 等字段
     */
    public static final Consumer<Map<String, Object>> beforeUpdate = params -> {
        Object user = DataServiceUtils.getCurrentUser();
        long userId = DataServiceUtils.executeMethod(user, "getId", long.class);
        String userName = DataServiceUtils.executeMethod(user, "getName", String.class);

        params.put("updater_id", userId);
        params.put("updater", userName);
    };

    /**
     * 更新之前的执行的回调函数，可以设置 updateBy 等的字段
     * isHasIsDeleted = 根据是否有删除标记字段来构造不同的 SQL 语句
     * 这里只能拼凑 SQL 字符串了
     */
    public static final BiFunction<Boolean, String, String> beforeDelete = (Boolean isHasIsDeleted, String sql) -> {
        if (isHasIsDeleted) {
            Object user = DataServiceUtils.getCurrentUser();
            long userId = DataServiceUtils.executeMethod(user, "getId", long.class);
            String userName = DataServiceUtils.executeMethod(user, "getName", String.class);

            String s = "SET updater_id = " + userId + "," + " updater = '" + userName + "', ";
            sql = sql.replace("SET", s);
        }

        return sql;
    };
}