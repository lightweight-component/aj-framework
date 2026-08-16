package com.ajaxjs.security.referer;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * <a href="https://hengyun.tech/how-to-create-a-referer-filter/">...</a>
 * <pre>
 * 支持的配置项：
 * matchMethods   即拦截的方法，默认值"POST|PUT|DELETE|CONNECT|PATCH"，通常不用配置
 * allowSubDomainHosts 匹配子域名，以"|"分隔，如"test.com|abc.com"，
 *                     则http://test.com, http://xxx.test.com这样的请求都会匹配到，推荐优先使用这个配置
 * completeMatchHosts 完全匹配的域名，以"|"分隔，如"test.com|abc.com"，则只有http://test.com 这样的请求会匹配
 *                    像http://www.test.com 这样的请求不会被匹配
 *
 * responseError  被拦截的请求的response的返回值，默认是403
 * redirectPath   被拦截的请求重定向到的url，如果配置了这个值，则会忽略responseError的配置。
 *                    比如可以配置重定向到自己定义的错误页： /referer_error.html
 * bAllowEmptyReferer  是否允许空referer，默认是false，除非很清楚，否则不要改动这个
 * bAllowLocalhost   是否允许localhost, 127.0.0.1 这样的referer的请求，默认是true，便于调试
 * bAllowAllIPAndHost  是否允许本机的所有IP和host的referer请求，默认是false
 *
 * {@code
 * 	<filter>
 * 		<filter-name>refererFilter</filter-name>
 * 		<filter-class>com.test.RefererFilter</filter-class>
 * 		<init-param>
 * 			<param-name>completeMatchHosts</param-name>
 * 			<param-value>test.com|abc.com</param-value>
 * 		</init-param>
 * 		<init-param>
 * 			<param-name>allowSubDomainHosts</param-name>
 * 			<param-value>hello.com|xxx.yyy.com</param-value>
 * 		</init-param>
 * 	</filter>
 *
 * 	<filter-mapping>
 * 		<filter-name>refererFilter</filter-name>
 * 		<url-pattern>/*</url-pattern>
 * 	</filter-mapping>
 *    }
 * </pre>
 *
 * @author hengyunabc
 *
 */
@Slf4j
public class RefererFilter implements Filter {
    public static final String DEFAULT_MATHMETHODS = "POST|PUT|DELETE|CONNECT|PATCH";

    List<String> mathMethods = new ArrayList<>();

    boolean bAllowEmptyReferer = false;
    boolean bAllowLocalhost = true;
    boolean bAllowAllIPAndHost = false;

    /**
     * when bAllowSubDomain is true, allowHosts is "test.com", then
     * "www.test.com", "xxx.test.com" will be allow.
     */
    boolean bAllowSubDomain = false;
    String redirectPath = null;
    int responseError = HttpServletResponse.SC_FORBIDDEN;
    HashSet<String> completeMatchHosts = new HashSet<>();
    List<String> allowSubDomainHostList = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        mathMethods.addAll(getSplitStringList(filterConfig, "matchMethods", "\\|", DEFAULT_MATHMETHODS));
        completeMatchHosts.addAll(getSplitStringList(filterConfig, "completeMatchHosts", "\\|", ""));
        List<String> allowSubDomainHosts = getSplitStringList(filterConfig, "allowSubDomainHosts", "\\|", "");
        completeMatchHosts.addAll(allowSubDomainHosts);

        for (String host : allowSubDomainHosts) {
            // check the first char if is '.'
            if (!host.isEmpty() && host.charAt(0) != '.')
                allowSubDomainHostList.add("." + host);
            else
                allowSubDomainHostList.add(host);
        }

        responseError = getInt(filterConfig, "responseError", responseError);
        redirectPath = filterConfig.getInitParameter("redirectPath");
        bAllowEmptyReferer = getBoolean(filterConfig, "bAllowEmptyReferer", bAllowEmptyReferer);
        bAllowLocalhost = getBoolean(filterConfig, "bAllowLocalhost", bAllowLocalhost);

        if (bAllowLocalhost) {
            completeMatchHosts.add("localhost");
            completeMatchHosts.add("127.0.0.1");
            completeMatchHosts.add("[::1]");
        }

        bAllowAllIPAndHost = getBoolean(filterConfig, "bAllowAllIPAndHost", bAllowAllIPAndHost);

        if (bAllowAllIPAndHost)
            completeMatchHosts.addAll(getAllIPAndHost());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
            ServletException {
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            String method = request.getMethod();
            /*
             * if method not in POST|PUT|DELETE|CONNECT|PATCH, don't check
             * referrer.
             */
            if (!mathMethods.contains(method.trim().toUpperCase())) {
                filterChain.doFilter(request, response);
                return;
            }

            String referrer = request.getHeader("referer");

            boolean bAllow = false;

            if (isBlank(referrer))
                bAllow = bAllowEmptyReferer;
            else {
                URL url;

                try {
                    url = new URL(referrer);
                    String host = url.getHost();

                    if (completeMatchHosts.contains(host))
                        bAllow = true;
                    else {
                        for (String domain : allowSubDomainHostList) {
                            if (host.endsWith(domain)) {
                                bAllow = true;
                                break;
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    log.error("illegal referrer! referrer: {}", referrer, e);
                    bAllow = false;
                }
            }

            if (bAllow)
                filterChain.doFilter(request, response);
            else {
                if (isBlank(redirectPath))
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                else
                    response.sendRedirect(request.getContextPath() + redirectPath);
            }
        } else
            filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    private static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0)
            return true;

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i)))
                return false;
        }

        return true;
    }

    private static boolean getBoolean(FilterConfig filterConfig, String parameter, boolean defaultParameterValue) {
        String parameterString = filterConfig.getInitParameter(parameter);
        if (parameterString == null)
            return defaultParameterValue;

        return Boolean.parseBoolean(parameterString.trim());
    }

    private static int getInt(FilterConfig filterConfig, String parameter, int defaultParameterValue) {
        String parameterString = filterConfig.getInitParameter(parameter);
        if (parameterString == null)
            return defaultParameterValue;

        return Integer.parseInt(parameterString.trim());
    }

    /**
     * <pre>
     * getSplitStringList(filterConfig, "hosts", "\\|", "test.com|abc.com");
     *
     * if hosts is "hello.com|google.com", will return {"hello.com", google.com"}.
     * if hosts is null, will return {"test.com", "abc.com"}
     * </pre>
     */
    private static List<String> getSplitStringList(FilterConfig filterConfig, String parameter, String regex, String defaultParameterValue) {
        String parameterString = filterConfig.getInitParameter(parameter);
        if (parameterString == null)
            parameterString = defaultParameterValue;

        String[] split = parameterString.split("\\|");
        List<String> resultList = new LinkedList<>();

        for (String method : split)
            resultList.add(method.trim());

        return resultList;
    }

    public static Set<String> getAllIPAndHost() {
        Set<String> resultSet = new HashSet<>();
        Enumeration<NetworkInterface> interfaces;

        try {
            interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface nic = interfaces.nextElement();
                Enumeration<InetAddress> addresses = nic.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (address instanceof Inet4Address) {
                        resultSet.add(address.getHostAddress());
                        resultSet.add(address.getHostName());
                    } else if (address instanceof Inet6Address) {
                        // TODO how to process Inet6Address?
                        // resultSet.add("[" + address.getHostAddress() + "]");
                        // resultSet.add(address.getHostName());
                    }
                }
            }
        } catch (SocketException e) {
            log.error("getAllIPAndHost error!", e);
        }
        return resultSet;
    }
}