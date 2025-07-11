package com.ajaxjs.web.website.nginx;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public abstract class MatchNginxBaseFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        MatchNginxRequestWrapper paramsRequest = new MatchNginxRequestWrapper((HttpServletRequest) request);

        chain.doFilter(paramsRequest, response);
    }

    @Override
    public void init(FilterConfig arg0) {
    }

    @Override
    public void destroy() {
    }
}
