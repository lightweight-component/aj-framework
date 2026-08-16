[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-security?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-security)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.4-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/aj-security )
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-security)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![中文](https://img.shields.io/badge/lang-中文-red)](./README.zh-CN.md)


# AJ Security, A Small security library

Based on the Spring/HandlerInterceptor mechanism, this library abstracts a set of filtering/validation mechanisms, forming a unified invocation chain that is flexibly configurable and extensible. The architecture of this security framework is simple, the code is concise, and it has no additional dependencies, making it suitable for any Spring-based project. Spring Boot applications can use it out of the box by simply importing the jar package.

The features of this framework include:

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


The code of AJ Security is small, straightforward and well commented. There are not much third-party dependencies, just Java 8 requires.

For further details, please refer to the [User Manual](https://security.ajaxjs.com).

## Install

Runs on Java 8+. Maven:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-security</artifactId>
    <version>1.4</version>
</dependency>
```


## Source Code

Under Apache License v3.0.

- Github: [https://github.com/lightweight-component/aj-security](https://github.com/lightweight-component/aj-security)
- Gitcode: [https://gitcode.com/lightweight-component/aj-security](https://gitcode.com/lightweight-component/aj-security), for Chinese users faster access.

## Links

[User Manual](https://security.ajaxjs.com) | [JavaDoc](https://javadoc.io/doc/com.ajaxjs/aj-security) | [DeepWiki](https://deepwiki.com/lightweight-component/aj-security)