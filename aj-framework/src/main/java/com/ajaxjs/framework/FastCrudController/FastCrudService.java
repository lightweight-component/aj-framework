package com.ajaxjs.framework.FastCrudController;

import com.ajaxjs.framework.model.PageVO;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.sqlgenerator.AutoQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FastCrudService implements FastCrudController {
    @Autowired(required = false)
    Namespaces namespaces;

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
    public PageVO<Map<String, Object>> page(String namespace) {
        return null;
    }

    @Override
    public Long create(String namespace, Map<String, Object> params) {
        return null;
    }

    @Override
    public Long create(Map<String, Object> params, String namespace) {
        return null;
    }

    @Override
    public Boolean update(String namespace, Map<String, Object> params) {
        return null;
    }

    @Override
    public Boolean update(Map<String, Object> params, String namespace) {
        return null;
    }

    @Override
    public Boolean delete(String namespace, Long id) {
        return null;
    }
}
