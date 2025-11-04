package com.ajaxjs.framework.dataservice;

import com.ajaxjs.framework.dataservice.model.ActionType;
import com.ajaxjs.framework.dataservice.model.Endpoint;
import com.ajaxjs.framework.dataservice.model.Group;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.httpremote.HttpConstant;

import java.util.List;

public class TestCase {
    public static EndpointMgr initEndpointMgr() {
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

        Endpoint endpoint3 = new Endpoint();
        endpoint3.setId(4);
        endpoint3.setGroupId(1);
        endpoint3.setUrl("/patch");
        endpoint3.setActionType(ActionType.CREATE);
        endpoint3.setTableName("shop_address");
        endpoint3.setAutoIns(true);
        endpoint3.setAutoSql(true);
        endpoint3.setMethod(HttpConstant.HttpMethod.POST);

        Endpoint endpoint4 = new Endpoint();
        endpoint4.setId(5);
        endpoint4.setGroupId(1);
        endpoint4.setUrl("/patch");
        endpoint4.setActionType(ActionType.UPDATE);
        endpoint4.setTableName("shop_address");
        endpoint4.setIdField("id");
        endpoint4.setAutoSql(true);
//        endpoint3.setSql("select * from shop_address where id = ?");
        endpoint4.setMethod(HttpConstant.HttpMethod.PUT);

        Endpoint endpoint5 = new Endpoint();
        endpoint5.setId(6);
        endpoint5.setGroupId(1);
        endpoint5.setUrl("/update/{id}");
        endpoint5.setActionType(ActionType.UPDATE);
        endpoint5.setTableName("shop_address");
        endpoint5.setIdField("id");
        endpoint5.setAutoSql(false);
        endpoint5.setSql("UPDATE shop_address SET name = #{name} where id = ?");
        endpoint5.setMethod(HttpConstant.HttpMethod.PUT);

        Endpoint endpoint6 = new Endpoint();
        endpoint6.setId(7);
        endpoint6.setGroupId(1);
        endpoint6.setUrl("/create");
        endpoint6.setActionType(ActionType.CREATE);
        endpoint6.setTableName("shop_address");
        endpoint6.setAutoSql(false);
        endpoint6.setAutoIns(true);
        endpoint6.setSql("INSERT INTO shop_address (name, address) VALUES (#{name}, #{address});");
        endpoint6.setMethod(HttpConstant.HttpMethod.POST);

        List<Endpoint> endpoints = ObjectHelper.listOf(endpoint, endpoint1,
                endpoint2, endpoint3, endpoint4, endpoint5, endpoint6, page(), delete());

        return EndpointMgr.init(groups, endpoints);
    }

    private static Endpoint page() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(8);
        endpoint.setGroupId(1);
        endpoint.setUrl("/page");
        endpoint.setActionType(ActionType.PAGE_LIST);
        endpoint.setSql("select * from shop_address");
        endpoint.setMethod(HttpConstant.HttpMethod.GET);

        return endpoint;
    }

    private static Endpoint delete() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(9);
        endpoint.setGroupId(1);
        endpoint.setUrl("/del");
        endpoint.setActionType(ActionType.DELETE);
        endpoint.setSql("update shop_address set name = 'hi' where id = 2");
        endpoint.setMethod(HttpConstant.HttpMethod.DELETE);

        return endpoint;
    }

}
