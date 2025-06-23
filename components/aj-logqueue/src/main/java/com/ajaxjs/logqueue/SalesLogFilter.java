package com.ajaxjs.logqueue;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;

/**
 * 消息入队可以在任何需要保存日志的地方操作，如aop统一拦截日志处理，filter过滤请求日志处理，或者耦合的业务日志，记住，不阻塞入队操作，不然将影响正常的业务操作，如下为filter统一处理请求日志
 */
public class SalesLogFilter implements Filter {

    private RoleResourceService resourceService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext context = filterConfig.getServletContext();
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
        resourceService = ctx.getBean(RoleResourceService.class);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String requestUrl = request.getRequestURI();
            String requestType = request.getMethod();
            String ipAddress = HttpClientUtil.getIpAddr(request);
            Map resource = resourceService.getResource();
            String context = resource.get(requestUrl);

            //动态url正则匹配
            if (StringUtil.isNull(context)) {
                for (Map.Entry entry : resource.entrySet()) {
                    String resourceUrl = entry.getKey();
                    if (requestUrl.matches(resourceUrl)) {
                        context = entry.getValue();
                        break;
                    }
                }
            }
            SalesLog log = new SalesLog();
            log.setCreateDate(new Timestamp(System.currentTimeMillis()));
            log.setContext(context);
            log.setOperateUser(UserTokenUtil.currentUser.get().get("realname"));
            log.setRequestIp(ipAddress);
            log.setRequestUrl(requestUrl);
            log.setRequestType(requestType);
            SalesLogQueue.getInstance().push(log);
        } catch (Exception e) {
            e.printStackTrace();
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}