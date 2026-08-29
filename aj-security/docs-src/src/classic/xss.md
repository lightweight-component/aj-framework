---
title: Prevent XSS Cross-Site Scripting Attacks
subTitle: 2024-12-05 by Frank Cheung
description: Prevent XSS Cross-Site Scripting Attacks
date: 2022-01-05
tags:
  - XSS
layout: layouts/docs.njk
---

# General Web Validation

General web validation primarily addresses the prevention of attacks such as XSS, CSRF, and CRLF. Unlike other
components within this framework that rely on Spring Interceptors, the validation mechanism here is implemented using
traditional Servlet filters.

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
    
            chain.doFilter(securityRequest, securityResponse);// Continue processing the request
        } 
        else
            chain.doFilter(request, response);
}
```

By overriding the Servlet's method in this way, even if the Java system is attacked or compromised, the filter can still
effectively prevent XSS attacks.

# Prevent XSS Cross-Site Scripting Attacks

XSS (Cross-Site Scripting) is a type of attack where malicious code (usually JavaScript) is injected into a web
application. Attackers exploit XSS vulnerabilities to steal user data, hijack sessions, or manipulate browser behavior.

The key to preventing XSS attacks is to strictly validate and escape user inputs, ensuring that any dynamic content is
not interpreted as executable code before being output to a page.

## Configuration

First, ensure the filter is enabled by setting `enabled: true`, then activate `xssCheck` to enable detection.

```yaml
security:
    web: # General attack prevention
        enabled: true
        xssCheck: true # Prevent XSS Cross-Site Scripting Attacks
```