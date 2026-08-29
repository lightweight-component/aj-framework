---
title: Home
subTitle: 
description: AJ Security Introduction
date: 2025-07-01
tags:
  -  AJ Security Introduction
layout: layouts/docs.njk
---

# AJ Security: A Practical Java Web Security Library

Based on the Spring/HandlerInterceptor mechanism, this library abstracts a set of filtering/validation mechanisms,
forming a unified invocation chain that is flexibly configurable and extensible. The architecture of this security
framework is simple, the code is concise, and it has no additional dependencies, making it suitable for any Spring-based
project. Spring Boot applications can use it out of the box by simply importing the jar package.

The features of this framework include:

<style>
  table, table td { 
    border: 0!important;
  }
  table td {
    text-align: left;
    vertical-align: top;
  }
</style>
<table><tr><td>

- HTTP Web Security
    - HTTP Referer validation
    - Timestamp encrypted token validation
    - IP whitelist/blacklist
    - Prevention of duplicate submissions
    - IP region-based restrictions (TODO)
- General Web Validation
    - XSS attack prevention
    - CRLF attack prevention
    - Cookie size check
- HTTP Standard Authentication
    - HTTP Basic Auth authentication
    - HTTP Digest Auth authentication

</td>

<td>

- Captcha Mechanisms
    - Simple Java image captcha
    - Image captcha based on kaptcha
    - Captcha based on Google Recaptcha
    - Captcha based on CloudFlare Turnstile

- API Interface Features
    - Rate limiting and quota (TODO)
- Other Practical Features
    - Entity field desensitization
    - API interface encryption/decryption

</td></tr></table>

The code of AJ Security is small(A JAR of approximately 100 KB), straightforward and well commented. There are not much
third-party dependencies, just
Java 8 requires.

## Source Code

Under Apache License v3.0.

- Github: [https://github.com/lightweight-component/aj-security](https://github.com/lightweight-component/aj-security)
-

Gitcode: [https://gitcode.com/lightweight-component/aj-security](https://gitcode.com/lightweight-component/aj-security),
for Chinese users faster access.

## Links

[User Manual](https://security.ajaxjs.com) | [JavaDoc](https://javadoc.io/doc/com.ajaxjs/aj-security) | [DeepWiki](https://deepwiki.com/lightweight-component/aj-security)