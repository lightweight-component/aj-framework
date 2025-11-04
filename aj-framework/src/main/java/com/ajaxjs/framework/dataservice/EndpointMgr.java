package com.ajaxjs.framework.dataservice;

import com.ajaxjs.framework.dataservice.model.Endpoint;
import com.ajaxjs.framework.dataservice.model.Group;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.UrlHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A map containing all the endpoints.
 */
@Slf4j
public class EndpointMgr extends HashMap<String, Endpoint> {
    /**
     * Create a new endpoint manager by specified map size as an initial capacity.
     *
     * @param initialCapacity The size of this map.
     */
    public EndpointMgr(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Get the endpoint object by route
     *
     * @param route URL+HTTP Method
     * @return Endpoint object
     */
    public Endpoint get(String route) {
        Endpoint value = super.get(route);

        if (value == null) { // list all routes
            log.info("All the routes for debug:");
            keySet().forEach(log::info);

            throw new UnsupportedOperationException("The route: " + route + " is not found.");
        }

        return value;
    }

    /**
     * Initialize the dispatcher.
     * It can be done by SQL.
     * However, I want to modify some details that are hard to do in SQL.
     *
     * @param groups    List of groups.
     * @param endpoints List of endpoints.
     * @return EndpointMgr
     */
    public static EndpointMgr init(List<Group> groups, List<Endpoint> endpoints) {
        // list as a map
        Map<Integer, Group> groupMap = ObjectHelper.mapOf(groups.size());

        for (Group group : groups)
            groupMap.put(group.getId(), group);

        EndpointMgr endPointMgr = new EndpointMgr(ObjectHelper.getInitialCapacity(endpoints.size()));

        endpoints.forEach(endpoint -> {
            Group group = groupMap.get(endpoint.getGroupId());
            String prefix = group.getUrl();

            if (!prefix.startsWith("/")) // ensure that starts with /
                prefix = '/' + prefix;

            String url = UrlHelper.concatUrl(prefix, endpoint.getUrl());
            url += '#' + endpoint.getMethod().toString();
            log.info("Registering endpoint: " + url);
            endpoint.setUrlMethod(url);

            endPointMgr.put(url, endpoint);
        });

        return endPointMgr;
    }
}
