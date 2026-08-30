package com.ajaxjs.dataservice.fastcrud;

import com.ajaxjs.dataservice.fastcrud.dbconfig.AutoQueryBusinessConfig;
import com.ajaxjs.dataservice.fastcrud.dbconfig.NamespaceDataEntity;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.crud.page.PageResult;
import com.ajaxjs.sqlman.model.tablemodel.TableModel;
import com.ajaxjs.sqlman.sqlgenerator.AutoQuery;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * FastCRUD 命名空间到自动查询规则的注册表。
 */
@Slf4j
public class Namespaces extends HashMap<String, AutoQuery> {
    /**
     * 获取命名空间对应的自动查询定义。
     *
     * @param namespace 命名空间名称
     * @return 自动查询定义
     * @throws UnsupportedOperationException 当命名空间未注册时抛出
     */
    public AutoQuery get(String namespace) {
        AutoQuery autoQuery = super.get(namespace);

        if (autoQuery == null)
            throw new UnsupportedOperationException("The namespace your accessed [" + namespace + "] is not available");

        return autoQuery;
    }

    /**
     * 将对象转换为不包含空值属性的映射。
     *
     * @param bean 要转换的对象
     * @return 不含 {@code null} 值的属性映射
     */
    public static Map<String, Object> bean2map(Object bean) {
        Map<String, Object> map = JsonUtil.pojo2map(bean);

        // Use iterators remove method
        map.entrySet().removeIf(entry -> entry.getValue() == null);

        return map;
    }

    /**
     * 将记录映射列表转换为指定类型的对象列表。
     *
     * @param list 记录映射列表
     * @param clz 目标对象类型
     * @param <T> 目标对象类型
     * @return 转换后的对象列表；输入为空时返回 {@code null}
     */
    public static <T> List<T> listMap2lisBean(List<Map<String, Object>> list, Class<T> clz) {
        if (ObjectHelper.isEmpty(list))
            return null;

        List<T> beanList = new ArrayList<>(list.size());

        for (Map<String, Object> map : list)
            beanList.add(JsonUtil.map2pojo(map, clz));

        return beanList;
    }

    /**
     * 将分页记录中的映射转换为指定类型的对象，同时保留分页信息。
     *
     * @param page 原始分页结果
     * @param clz 目标对象类型
     * @param <T> 目标对象类型
     * @return 元素已转换的分页结果
     */
    public static <T> PageResult<T> pageListMap2lisBean(PageResult<Map<String, Object>> page, Class<T> clz) {
        List<Map<String, Object>> list = page.getList();
        List<T> beanList;

        if (ObjectHelper.isEmpty(list))
            beanList = null;
        else {
            beanList = new ArrayList<>(list.size());

            for (Map<String, Object> map : list)
                beanList.add(JsonUtil.map2pojo(map, clz));
        }

        PageResult<T> result = new PageResult<>();
        BeanUtils.copyProperties(page, result);
        result.setList(beanList);

        return result;
    }

    /**
     * 获取当前用户标识的回调。
     */
    Supplier<Serializable> getCurrentUserId;

    /**
     * 获取当前租户标识的回调。
     */
    Supplier<Serializable> getTenantId;

    /**
     * Load namespace from DB
     *
     * @param getCurrentUserId How to get current user id
     * @param getTenantId      How to get tenant id
     */
    public void loadFromDB(Supplier<Serializable> getCurrentUserId, Supplier<Serializable> getTenantId) {
        String sql = "SELECT * FROM ds_namespace WHERE stat != 1";

        this.getCurrentUserId = getCurrentUserId;
        this.getTenantId = getTenantId;

        try {
            List<NamespaceDataEntity> list = new Action(sql).query().list(NamespaceDataEntity.class);

            if (ObjectHelper.isEmpty(list))
                return;

            for (NamespaceDataEntity entity : list) {
                AutoQueryBusinessConfig config = new AutoQueryBusinessConfig(entity);
                config.setGetCurrentUserId(getCurrentUserId);
                config.setGetTenantId(getTenantId);

                TableModel tableModel = new TableModel();
                tableModel.setTableName(entity.getTableName());
                put(entity.getNamespace(), new AutoQuery(tableModel, config));
            }

            log.info("Load FastCRUD's namespace from DB successfully");
        } catch (Exception e) {
            log.warn("Load FastCRUD's namespace from DB failed: " + e.getMessage(), e);
            // avoid fails to spring startup and JDBC connection manually closing.
        }
    }

    /**
     * 使用上次保存的用户和租户供应器重新加载命名空间配置。
     */
    public void reload() {
        loadFromDB(getCurrentUserId, getTenantId);
    }
}
