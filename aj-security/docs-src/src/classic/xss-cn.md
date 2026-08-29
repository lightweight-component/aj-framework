---
title: 防止 XSS 跨站攻击
subTitle: 2024-12-05 by Frank Cheung
description: 防止 XSS 跨站攻击
date: 2022-01-05
tags:
  - XSS
layout: layouts/docs-cn.njk
---

# 一般性 Web 校验

一般性 Web 校验主要指出防范 XSS、CSRF、CRLF 等的攻击。与本框架内其他组件基于 Spring 拦截器不同，这里的校验机制基于传统
Servlet 的过滤器（Filter）实现。

```java
@Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isEnabled) {
            SecurityRequest securityRequest = new SecurityRequest((HttpServletRequest) request);
            SecurityResponse securityResponse = new SecurityResponse((HttpServletResponse) response);
            securityResponse.isCRLFCheck = isCRLFCheck;
            securityResponse.isCookiesSizeCheck = isCookiesSizeCheck;
            securityResponse.maxCookieSize = maxCookieSize;
    
            securityRequest.isXssCheck = securityResponse.isXssCheck = isXssCheck;
    
            chain.doFilter(securityRequest, securityResponse);// 继续处理请求
        } 
        else
            chain.doFilter(request, response);
}
```

这样重写 Servlet 的方法，即使 Java 系统被攻击入侵，仍然可以通过过滤器进行 XSS 攻击的防范。

# 防止 XSS 跨站攻击

XSS（Cross-Site Scripting，跨站脚本攻击）是一种通过向 Web 应用程序注入恶意代码（通常是 JavaScript）的攻击方式。攻击者利用 XSS
漏洞，窃取用户数据、劫持会话或操作用户浏览器行为。

防御 XSS 攻击的核心是对用户输入进行严格验证和转义，确保任何动态内容在输出到页面之前不会被解析为可执行代码。

## 配置

首先确保过滤器打开，即`enabled: true`，然后配置`xssCheck`打开检测。

```yaml
security:
    web: # 常规攻击
        enabled: true
        xssCheck: true # 防止 XSS 跨站攻击
```