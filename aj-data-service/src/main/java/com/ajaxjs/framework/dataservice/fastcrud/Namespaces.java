package com.ajaxjs.framework.dataservice.fastcrud;

import com.ajaxjs.sqlman.sqlgenerator.AutoQuery;
import com.ajaxjs.util.JsonUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Namespaces extends HashMap<String, AutoQuery> {
    public AutoQuery get(String namespace) {
        AutoQuery autoQuery = super.get(namespace);

        if (autoQuery == null)
            throw new UnsupportedOperationException("The namespace your accessed " + namespace + " is not available");

        return autoQuery;
    }

    public static Map<String, Object> bean2map(Object bean) {
        Map<String, Object> map = JsonUtil.pojo2map(bean);
        Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();

            if (entry.getValue() == null)
                iterator.remove(); // Use iterators remove method
        }

        return map;
    }
}
