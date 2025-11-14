package com.ajaxjs.framework.dataservice.fastcrud;

import com.ajaxjs.framework.dataservice.fastcrud.dbconfig.AutoQueryBusinessConfig;
import com.ajaxjs.framework.dataservice.fastcrud.dbconfig.NamespaceDataEntity;
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

@Slf4j
public class Namespaces extends HashMap<String, AutoQuery> {
    public AutoQuery get(String namespace) {
        AutoQuery autoQuery = super.get(namespace);

        if (autoQuery == null)
            throw new UnsupportedOperationException("The namespace your accessed [" + namespace + "] is not available");

        return autoQuery;
    }

    public static Map<String, Object> bean2map(Object bean) {
        Map<String, Object> map = JsonUtil.pojo2map(bean);

        // Use iterators remove method
        map.entrySet().removeIf(entry -> entry.getValue() == null);

        return map;
    }

    public static <T> List<T> listMap2lisBean(List<Map<String, Object>> list, Class<T> clz) {
        if (ObjectHelper.isEmpty(list))
            return null;

        List<T> beanList = new ArrayList<>(list.size());

        for (Map<String, Object> map : list)
            beanList.add(JsonUtil.map2pojo(map, clz));

        return beanList;
    }

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
     * Load namespace from DB
     *
     * @param getCurrentUserId How to get current user id
     * @param getTenantId      How to get tenant id
     */
    public void loadFromDB(Supplier<Serializable> getCurrentUserId, Supplier<Serializable> getTenantId) {
        try {
            String sql = "SELECT * FROM ds_namespace WHERE stat != 1";
            List<NamespaceDataEntity> list = new Action(sql).query().list(NamespaceDataEntity.class);

            if (ObjectHelper.isEmpty(list))
                return;

            for (NamespaceDataEntity entity : list) {
                AutoQueryBusinessConfig config = new AutoQueryBusinessConfig(entity);
                config.setGetCurrentUserId(getCurrentUserId);
                config.setGetTenantId(getTenantId);

                TableModel tableModel = new TableModel();
                tableModel.setTableName(entity.getTableName());
                log.info("Load namespace: " + entity.getNamespace());
                put(entity.getNamespace(), new AutoQuery(tableModel, config));
            }

            log.info("Load FastCRUD's namespace from DB successfully");
        } catch (Exception e) {
            log.warn("Load FastCRUD's namespace from DB failed: " + e.getMessage(), e);
            // avoid fails to spring startup and JDBC connection manually closing.
        }
    }
}
