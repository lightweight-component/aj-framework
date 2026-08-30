package com.ajaxjs.dataservice.fastcrud.dbconfig;

import com.ajaxjs.sqlman.sqlgenerator.AutoQueryBusiness;
import com.ajaxjs.sqlman.sqlgenerator.TableJoin;
import com.ajaxjs.util.JsonUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 基于数据库命名空间配置实现的 FastCRUD 业务规则。
 */
@RequiredArgsConstructor
@Data
public class AutoQueryBusinessConfig implements AutoQueryBusiness {
    /**
     * 命名空间的持久化配置。
     */
    final NamespaceDataEntity entity;

    /**
     * 当前用户标识供应器。
     */
    Supplier<Serializable> getCurrentUserId;

    /**
     * 当前租户标识供应器。
     */
    Supplier<Serializable> getTenantId;

    @Override
    public boolean isListOrderByDate() {
        return entity.getListOrderByDate() != null && entity.getListOrderByDate();
    }

    @Override
    public boolean isTenantIsolation() {
        return entity.getTenantIsolation() != null && entity.getTenantIsolation();
    }

    @Override
    public boolean isCurrentUserOnly() {
        return entity.getCurrentUserOnly() != null && entity.getCurrentUserOnly();
    }

    @Override
    public boolean isFilterDeleted() {
        return entity.getFilterDeleted() != null && entity.getFilterDeleted();
    }

    @Override
    public Serializable getCurrentUserId() {
        return getCurrentUserId.get();
    }

    @Override
    public Serializable getTenantId() {
        return getTenantId.get();
    }

    @Override
    public TableJoin getTableJoin() {
        Map<String, Object> tableJoinMap = entity.getTableJoin();

        return tableJoinMap == null ? null : JsonUtil.map2pojo(tableJoinMap, TableJoin.class);
    }

    @Override
    public Integer getSaveUserOnCreate() {
        return entity.getSaveUserOnCreate();
    }
}
