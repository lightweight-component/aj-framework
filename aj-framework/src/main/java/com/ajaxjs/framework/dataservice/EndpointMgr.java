package com.ajaxjs.framework.dataservice;

import com.ajaxjs.framework.dataservice.model.Endpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class EndpointMgr extends HashMap<String, Endpoint> {
    public EndpointMgr(int initialCapacity) {
        super(initialCapacity);
    }

    public Endpoint get(String route) {
        Endpoint value = super.get(route);

        if (value == null) {
            log.info("All the routes:");
            keySet().forEach(log::info);

            throw new UnsupportedOperationException("The route: " + route + " is not found.");
        }

        return value;
    }
}
