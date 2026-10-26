package com.ajaxjs.security.antireptile.util;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 为拦截页面和验证码接口设置固定跨域响应头的工具。
 * <p>方法仅写入现有响应，不解析请求来源，也不单独处理预检请求。
 * 通配来源与允许携带凭据是否被浏览器接受，仍受浏览器跨域策略约束。</p>
 */
public class CrosUtil {
    /**
     * 允许的跨域请求头列表，直接作为 Access-Control-Allow-Headers 的值。
     */
    private static final String ALLOWED_HEADERS = "x-requested-with, authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN,token,username,client";

    /**
     * 允许的跨域请求方法，当前值为通配符。
     */
    private static final String ALLOWED_METHODS = "*";

    /**
     * 允许的跨域来源，当前值为通配符。
     */
    private static final String ALLOWED_ORIGIN = "*";

    /**
     * 允许前端读取的响应头，当前值为通配符。
     */
    private static final String ALLOWED_EXPOSE = "*";

    /**
     * 预检缓存时间的响应头原始值，当前直接写入字符串 18000L。
     */
    private static final String MAX_AGE = "18000L";

    /**
     * 将组件预设的跨域响应头写入响应，覆盖已有同名头。
     * <p>包括允许来源、方法、请求头、暴露头、预检缓存时间及凭据标记；
     * 各值直接采用类中常量，不根据请求动态调整。</p>
     *
     * @param response 待添加跨域头的非空 HTTP 响应
     */
    public static void setCrosHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        response.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
        response.setHeader("Access-Control-Max-Age", MAX_AGE);
        response.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        response.setHeader("Access-Control-Expose-Headers", ALLOWED_EXPOSE);
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
