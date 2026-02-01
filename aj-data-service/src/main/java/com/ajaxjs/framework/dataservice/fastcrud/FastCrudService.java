package com.ajaxjs.framework.dataservice.fastcrud;

import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.crud.page.PageResult;
import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.sqlgenerator.AutoQuery;
import com.ajaxjs.sqlman.sqlgenerator.AutoQueryBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service
public class FastCrudService implements FastCrudController {
    @Autowired(required = false)
    protected Namespaces namespaces;

    @Override
    public Map<String, Object> info(String namespace, Long id) {
        AutoQuery autoQuery = namespaces.get(namespace);
        String sql = autoQuery.info();

        return new Action(sql).query(id).one();
    }

    @Override
    public List<Map<String, Object>> list(String namespace) {
        String where = Tools.getWhereClause();
        AutoQuery autoQuery = namespaces.get(namespace);
        String sql = autoQuery.list(where);

        return new Action(sql).query().list();
    }

    @Override
    public PageResult<Map<String, Object>> page(String namespace) {
        String where = Tools.getWhereClause();
        AutoQuery autoQuery = namespaces.get(namespace);
        String sql = autoQuery.list(where);

        return new Action(sql).query().pageByStartLimit(DiContextUtil.getRequest());
    }

    @Override
    public PageResult<Map<String, Object>> pageByNo(String namespace) {
        String where = Tools.getWhereClause();
        AutoQuery autoQuery = namespaces.get(namespace);
        String sql = autoQuery.list(where);

        return new Action(sql).query().pageByPageNo(DiContextUtil.getRequest());
    }

    @Override
    public CreateResult<Serializable> create(String namespace, Map<String, Object> params) {
        return create(params, namespace);
    }

    @Override
    public CreateResult<Serializable> create(Map<String, Object> params, String namespace) {
        AutoQuery autoQuery = namespaces.get(namespace);
        String tableName = autoQuery.getTableModel().getTableName();
        AutoQueryBusiness autoQueryBusiness = autoQuery.getAutoQueryBusiness();
        Integer saveUserOnCreate = autoQueryBusiness.getSaveUserOnCreate();

        if (saveUserOnCreate != null) {
            Serializable userId = autoQueryBusiness.getCurrentUserId();

            if (testBCD(1, saveUserOnCreate) && !params.containsKey("creator_id"))
                params.put("creator_id", userId);

            if (testBCD(4, saveUserOnCreate) && !params.containsKey("user_id"))
                params.put("user_id", userId);

            // creator/user_name? TODO
        }

        return new Action(params, tableName).create().execute(autoQuery.getTableModel().isAutoIns());
    }

    @Override
    public UpdateResult update(String namespace, Map<String, Object> params) {
        return update(params, namespace);
    }

    @Override
    public UpdateResult update(Map<String, Object> params, String namespace) {
        AutoQuery autoQuery = namespaces.get(namespace);
        String tableName = autoQuery.getTableModel().getTableName();

        return new Action(params, tableName).update().withId(autoQuery.getTableModel().getIdField());
    }

    @Override
    public boolean deletePhysical(String namespace, Long id) {
        AutoQuery autoQuery = namespaces.get(namespace);
        String sql = autoQuery.deletePhysicalById();

        return new Action(sql).update(id).execute().isOk();
    }

    @Override
    public boolean deleteLogical(String namespace, Long id) {
        AutoQuery autoQuery = namespaces.get(namespace);
        String sql = autoQuery.deleteLogicalById();

        return new Action(sql).update(id).execute().isOk();
    }

    /**
     * 测试 8421 码是否包含 v
     *
     * @param v   当前权限值
     * @param all 总值
     * @return true=已包含
     */
    public static boolean testBCD(int v, int all) {
        return (v & all) == v;
    }
}
