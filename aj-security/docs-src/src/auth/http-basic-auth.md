---
title: HTTP 基本认证（Basic Auth）
subTitle: 2024-12-05 by Frank Cheung
description: Architecture
date: 2022-01-05
tags:
  - HTTP Basic Auth
layout: layouts/docs.njk
---

# HTTP Basic Authentication (Basic Auth)

**HTTP Basic Authentication (Basic Auth)** is the simplest HTTP authentication method. The client performs identity
verification by including the username and password (Base64 encoded) in the HTTP request header. It is commonly used in
test environments, internal systems, or simple interface protection (it is recommended to use HTTPS in production
environments, otherwise the password can be easily stolen).

## Basic Principle

1. The client encodes `username:password` using Base64.
2. Adds the following to the request header: `Authorization: Basic <Base64 encoded string>`
3. The server receives the request, decodes it, and verifies the username and password.

# Usage

## yaml Configuration

```yaml
security:
  HttpDigestAuth: # HTTP Digest Authentication
        globalCheck: true # Global check
        enabled: true
        username: admin
        password: admin
```

## Interceptor Validation

Add the `@HttpDigestAuthCheck` annotation to the interface in use:

```java
@GetMapping("/HttpDigestAuthCheck")
@HttpDigestAuthCheck
int HttpDigestAuthCheck();
```

The browser will prompt for account and password input.

![](/auth/auth.jpg)