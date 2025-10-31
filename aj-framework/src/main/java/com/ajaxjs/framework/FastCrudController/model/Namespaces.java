package com.ajaxjs.framework.FastCrudController.model;

import com.ajaxjs.sqlman_v2.sqlgenerator.AutoQuery;

import java.util.HashMap;

public class Namespaces extends HashMap<String, AutoQuery> {
    public AutoQuery get(String namespace) {
        AutoQuery autoQuery = super.get(namespace);

        if (autoQuery == null)
            throw new UnsupportedOperationException("The namespace your accessed " + namespace + " is not available");

        return autoQuery;
    }
}
