package org.example.controller;

import com.ajaxjs.framework.dataservice.Dispatcher;
import com.ajaxjs.framework.dataservice.EndpointMgr;
import com.ajaxjs.framework.dataservice.model.Endpoint;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.crud.Update;
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(DataServiceDispatcher.URL_PREFIX)
public class DataServiceDispatcher {
    static final String URL_PREFIX = "/ds_api";

    static boolean run;

    private final static AntPathMatcher ROUTER_MATCHER = new AntPathMatcher();

    @RequestMapping("/**")
    public Object request(HttpServletRequest req) {
        if (!run) {
            String sql = "CREATE TABLE shop_address (\n" +
                    "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    name VARCHAR(255) NOT NULL,\n" +
                    "    address VARCHAR(255) NOT NULL,\n" +
                    "    phone VARCHAR(20),\n" +
                    "    receiver VARCHAR(255),\n" +
                    "    stat INT,\n" +
                    "    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" +
                    ");";
            Action action = new Action(sql);
            Update update = new Update(action);
            UpdateResult execute = update.execute();

            log.info("Executed: {}", execute);

            action.setSql("INSERT INTO shop_address (name, address, phone, receiver, stat)\n" +
                    "VALUES\n" +
                    "('Shop A', '123 Main St', '123-456-7890', 'John Doe', 0),\n" +
                    "('Shop B', '456 Elm St', '234-567-8901', 'Jane Smith',0),\n" +
                    "('Shop C', '789 Oak St', '345-678-9012', 'Alice Johnson', 0),\n" +
                    "('Shop D', '101 Maple St', '456-789-0123', 'Bob Brown', 1),\n" +
                    "('Shop E', '202 Birch St', '567-890-1234', 'Charlie Davis', 1);");
            new Update(action).update();

            run = true;
        }

        String requestUri = req.getRequestURI();
        String contextPath = req.getContextPath();
        // Find where the actual path starts
        int pathOffset = (contextPath + URL_PREFIX).length();
        String remainingPath = requestUri.substring(pathOffset);

        log.info("Full URI: {}", requestUri);
        log.info("Remaining path after /ds_api: {}", remainingPath); // e.g., /users/list

        Dispatcher dispatcher = new Dispatcher();
        EndpointMgr endPointMgr = dispatcher.init();

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

        Map<String, String> queryStringMap = getQueryStringParams(req);

        switch (endpoint.getActionType()) {
            case INFO:
                Action action = new Action(endpoint.getSql());

                if (patchParams == null)
                    result = action.query(queryStringMap).one();
                else {
                    Collection<String> values = patchParams.values();
                    Object[] arr;

                    if (queryStringMap.size() > 0) {
                        List<Object> list = new ArrayList<>();
                        list.add(queryStringMap);
                        list.addAll(values);
                        arr = list.toArray();
                    } else
                        arr = values.toArray();

                    result = action.query(arr).one();
                }
                break;
            case LIST:
                result = new Action(endpoint.getSql()).query().list();
                break;
            case PAGE_LIST:
//                result = new Action(endpoint.getSql()).query().pageList();
                break;
            case CREATE:
                break;
            case UPDATE:
                break;
            case DELETE:
                break;
        }


        return result;
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

    /**
     * TODO: how to protect from SQL injection
     *
     * @param req HttpServletRequest
     * @return The parameters from query string
     */
    public static Map<String, String> getQueryStringParams(HttpServletRequest req) {
        Map<String, String[]> paramMap = req.getParameterMap(); // 获取所有参数
        Map<String, String> params = ObjectHelper.mapOf(paramMap.size());

        paramMap.forEach((key, values) -> {
            String value = values[0];// 只取第一个值
            value = value.replaceAll("\\s+", CommonConstant.EMPTY_STRING); // remove whitespace for avoiding SQL injection
            params.put(key, value);
        });

        return params;
    }
}