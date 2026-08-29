---
title: HTTP Digest Authentication
subTitle: 2024-12-05 by Frank Cheung
description: HTTP Digest Authentication
date: 2022-01-05
tags:
  - HTTP Digest
layout: layouts/docs.njk
---

# HTTP Digest Authentication

HTTP Digest Auth is similar to Basic Auth, but it does not transmit the plain text password directly.
Instead, it mixes the password, nonce, request method, etc., to generate a digest (hash) and transmits the hash value.
The advantage is that even if eavesdropped, the password cannot be obtained directly, and replay attacks are prevented.

# Usage

## yaml Configuration

```yaml
security:
    HttpBasicAuth: # HTTP Basic Authentication
        globalCheck: true # Global check
        enabled: true
        username: admin
        password: admin
```

## Interceptor Validation

Add the `@HttpBasicAuthCheck` annotation to the interface in use:

```java
@GetMapping("/HttpBasicAuthCheck")
@HttpBasicAuthCheck
int HttpBasicAuthCheck();
```

The browser will prompt for account and password input.

![](/auth/auth.jpg)