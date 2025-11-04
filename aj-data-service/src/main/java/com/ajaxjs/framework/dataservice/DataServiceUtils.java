package com.ajaxjs.framework.dataservice;

import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.ObjectHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Data service utils
 */
public class DataServiceUtils {
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
            String value = values.length > 0 ? values[0] : CommonConstant.EMPTY_STRING;// 只取第一个值
            value = value.replaceAll("\\s+", CommonConstant.EMPTY_STRING); // remove whitespace for avoiding SQL injection
            params.put(key, value);
        });

        return params;
    }
}
