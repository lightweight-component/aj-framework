package com.ajaxjs.framework.dataservice;

import com.ajaxjs.framework.dataservice.constant.ActionType;
import com.ajaxjs.framework.dataservice.model.Endpoint;
import com.ajaxjs.framework.dataservice.model.Group;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.UrlHelper;
import com.ajaxjs.util.httpremote.HttpConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class Dispatcher {
    public EndpointMgr init(List<Group> groups, List<Endpoint> endpoints) {
        // list as map
        Map<Integer, Group> groupMap = ObjectHelper.mapOf(groups.size());

        for (Group group : groups)
            groupMap.put(group.getId(), group);

        EndpointMgr endPointMgr = new EndpointMgr(ObjectHelper.getInitialCapacity(endpoints.size()));

        endpoints.forEach(endpoint -> {
            Group group = groupMap.get(endpoint.getGroupId());
            String prefix = group.getUrl();

            if (!prefix.startsWith("/")) // ensure starts with /
                prefix = '/' + prefix;

            String url = UrlHelper.concatUrl(prefix, endpoint.getUrl());
            url += '#' + endpoint.getMethod().toString();
            log.info("Registering endpoint: " + url);
            endpoint.setUrlMethod(url);

            endPointMgr.put(url, endpoint);
        });

        return endPointMgr;
    }

    public EndpointMgr init() {
        Group group = new Group();
        group.setId(1);
        group.setUrl("/foo");

        List<Group> groups = ObjectHelper.listOf(group);

        Endpoint endpoint = new Endpoint();
        endpoint.setId(1);
        endpoint.setGroupId(1);
        endpoint.setUrl("/bar");
        endpoint.setActionType(ActionType.INFO);
        endpoint.setSql("select * from shop_address where id = ${id}");
        endpoint.setMethod(HttpConstant.HttpMethod.GET);

        Endpoint endpoint1 = new Endpoint();
        endpoint1.setId(2);
        endpoint1.setGroupId(1);
        endpoint1.setUrl("/");
        endpoint1.setActionType(ActionType.LIST);
        endpoint1.setSql("select * from shop_address");
        endpoint1.setMethod(HttpConstant.HttpMethod.GET);

        Endpoint endpoint2 = new Endpoint();
        endpoint2.setId(3);
        endpoint2.setGroupId(1);
        endpoint2.setUrl("/patch/{id}");
        endpoint2.setActionType(ActionType.INFO);
        endpoint2.setSql("select * from shop_address where id = ?");
        endpoint2.setMethod(HttpConstant.HttpMethod.GET);


        List<Endpoint> endpoints = ObjectHelper.listOf(endpoint, endpoint1, endpoint2);

        return init(groups, endpoints);
    }
}
