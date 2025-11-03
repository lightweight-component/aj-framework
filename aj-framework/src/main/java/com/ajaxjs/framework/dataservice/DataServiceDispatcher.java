package com.ajaxjs.framework.dataservice;

import com.ajaxjs.framework.dataservice.model.Endpoint;
import com.ajaxjs.sqlman.JdbcConnection;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.crud.Query;
import com.ajaxjs.sqlman_v2.crud.Update;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping(DataServiceDispatcher.URL_PREFIX)
public abstract class DataServiceDispatcher {
    static final String URL_PREFIX = "/ds_api";

    private final static AntPathMatcher ROUTER_MATCHER = new AntPathMatcher();

    @Autowired
    EndpointMgr endPointMgr;

    @RequestMapping("/**")
    public Object request(HttpServletRequest req) {
        String requestUri = req.getRequestURI();
        String contextPath = req.getContextPath();
        // Find where the actual path starts
        int pathOffset = (contextPath + URL_PREFIX).length();
        String remainingPath = requestUri.substring(pathOffset);

        log.info("Full URI: {}", requestUri);
        log.info("Remaining path after /ds_api: {}", remainingPath); // e.g., /users/list

        String httpMethod = req.getMethod();
        String route = remainingPath + '#' + httpMethod;
        Endpoint endpoint = null;
        Map<String, String> patchParams = null;

        if (endPointMgr.containsKey(route))  // first hit
            endpoint = endPointMgr.get(route);
        else {
            String remainingPathWithMethod = remainingPath + '#' + httpMethod;

            for (String _route : endPointMgr.keySet()) {
                if (ROUTER_MATCHER.match(_route, remainingPathWithMethod)) {
                    log.info("Second hit :{}, the real: {}", _route, remainingPathWithMethod);
                    endpoint = endPointMgr.get(_route);
                    patchParams = ROUTER_MATCHER.extractUriTemplateVariables(_route, remainingPathWithMethod);

                    break;
                }
            }
        }

        if (endpoint == null)
            throw new UnsupportedOperationException("The route: " + route + " is not found.");

        log.info("endpoint: {}", endpoint);
        Object result = null;

        Map<String, String> mapParams = DataServiceUtils.getQueryStringParams(req);
        Action action;

        switch (endpoint.getActionType()) {
            case INFO:
                action = new Action(endpoint.getSql());
                result = actionQuery(action, patchParams, mapParams).one();

                break;
            case LIST:
                action = new Action(endpoint.getSql());
                result = actionQuery(action, patchParams, mapParams).list();

                break;
            case PAGE_LIST:
//                result = new Action(endpoint.getSql()).query().pageList();
                break;
            case CREATE:
                result = new WriteData(req, endpoint).setPatchParams(patchParams).create();
                break;
            case UPDATE:
                result = new WriteData(req, endpoint).setPatchParams(patchParams).update(endpoint.getIdField());
                break;
            case DELETE:
                if (endpoint.isAutoIns())
                    return actionUpdate(new Action(JdbcConnection.getConnection()), patchParams, mapParams).update();
                else { // plain SQL
                    String sql = endpoint.getSql();

                    return WriteData.combineParamsUpdate(new Action(sql), patchParams, mapParams).execute();
                }
        }

        if (result == null)
            result = new Empty();

        return result;
    }

    /**
     * To deal with the query string and patch params, make them as one array.
     */
    Query actionQuery(Action action, Map<String, String> patchParams, Map<String, String> mapParams) {
        if (patchParams == null)
            if (mapParams.size() > 0)
                return action.query(mapParams);
            else
                return action.query();
        else {
            Object[] arr = getQueryParams(mapParams, patchParams);

            return action.query(arr);
        }
    }

    Update actionUpdate(Action action, Map<String, String> patchParams, Map<String, String> mapParams) {
        if (patchParams == null)
            if (mapParams.size() > 0)
                return action.update(mapParams);
            else
                return action.update();
        else {
            Object[] arr = getQueryParams(mapParams, patchParams);

            return action.update(arr);
        }
    }

    static Object[] getQueryParams(Map<String, String> mapParams, Map<String, String> patchParams) {
        Collection<String> values = patchParams.values();
        Object[] arr;

        if (mapParams.size() > 0) {
            List<Object> list = new ArrayList<>();
            list.add(mapParams);
            list.addAll(values);
            arr = list.toArray();
        } else
            arr = values.toArray();

        return arr;
    }

    @Data
    class Empty {
        String msg = "No data";
    }

    /**
     * To get the URL path of Data service.
     * Removes the context path and the path of data service's controller, so the rest is the path of the endpoint.
     *
     * @param req HttpServletRequest
     * @return Remaining path
     */
    static String getRemainingPath(HttpServletRequest req) {
        String requestUri = req.getRequestURI();
        String contextPath = req.getContextPath();
        // Find where the actual path starts
        int pathOffset = (contextPath + URL_PREFIX).length();
        String remainingPath = requestUri.substring(pathOffset);
        log.info("Remaining path after /ds_api: {}", remainingPath); // e.g., /users/list

        return remainingPath;
    }
}