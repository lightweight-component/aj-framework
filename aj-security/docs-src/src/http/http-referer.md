---
title: HTTP Referer Validation
subTitle: 2024-12-05 by Frank Cheung
description: HTTP Referer 校验（也称为“Referer 检查”）是一种常见的 Web 安全措施，其原理是：后端服务器在接收请求时检查请求头中的 Referer 字段，判断请求来源是否为信任的域名或页面。
date: 2022-01-05
tags:
  - Referer
layout: layouts/docs.njk
---

# HTTP Referer Validation

HTTP Referer validation (also known as "Referer Check") is a common web security measure. Its principle is that the
backend server checks the Referer field in the request header when receiving a request to determine whether the request
source is a trusted domain or page.

## Basic Principle

1. When the client (browser) initiates an HTTP request, it includes a `Referer` in the request header, indicating the
   source page address of the request.
2. After the server receives the request, it reads the `Referer` and determines whether it is from a trusted source.
3. If the `Referer` does not meet the requirements, the request is rejected or an error is returned.

## Common Scenarios

- Source validation for sensitive operations such as form submission and API calls
- Preventing CSRF attacks
- Anti-leeching (e.g., allowing image or video resources to be accessed only from the same domain)

## Notes

- Not all requests include a Referer (e.g., direct URL input, certain browser privacy modes, HTTPS to HTTP)
- The Referer can be easily forged and should not be used as the sole security measure; it should only serve as a
  supplement
- It is recommended to combine with multiple measures such as CSRF Token and Cookie validation

# Usage

## yaml Configuration

```yaml
security:
  HttpReferer: # Referer Interceptor
    globalCheck: false # Global check
    enabled: true
    allowedReferrers:
      - https://example.com
      - https://another-example.com
      - https://my-site.com
```

## Interceptor Validation

Add the `@HttpRefererCheck` annotation to the interface in use:

```java
@GetMapping("/HttpRefererCheck")
@HttpRefererCheck
int HttpRefererCheck();
```