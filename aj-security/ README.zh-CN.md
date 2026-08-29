[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-security?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-security)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.3-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/aj-security)
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-security)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)

# AJ Security：实用的 Java Web 安全库

基于 Spring/HandlerInterceptor 拦截器机制，抽象一套过滤/校验的机制，形成统一的一套调用链，可灵活配置并扩展。本安全框架架构简单，代码精炼，没有其他额外的依赖，适用于任何基于
Spring 的项目。Spring Boot 程序引入 jar 包即可开箱即用。

本框架的功能有：

- HTTP Web 安全
    - HTTP Referer 校验
    - 时间戳加密 Token 校验
    - IP 白名单/黑名单
    - 防止重复提交数据
    - 根据 IP 地域限制（TODO）
- 一般性 Web 校验
    - 防止 XSS 跨站攻击
    - 防止 CRLF 攻击
    - Cookie 容量检查
- HTTP 标准认证
    - HTTP Basic Auth 认证
    - HTTP Digest Auth 认证
- 验证码 Captcha 机制
    - 简单 Java 图片验证码
    - 基于 kaptcha 的图片验证码
    - 基于 Google Recaptcha 的验证码
    - 基于 CloudFlare Turnstile 的验证码
- API 接口功能
    - 限流限次数（TODO）
- 其他实用功能
    - 实体字段脱敏
    - API 接口加解密

AJ Security 的代码量不多，而且都是很简单易懂的代码。另外没啥第三方依赖，适合 Java8+。

请参阅[用户手册](https://security.ajaxjs.com)以了解更多。

## 安装

需要 Java 8+. Maven:

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-security</artifactId>
    <version>1.4</version>
</dependency>
```

## 源代码

本项目采用 Apache License 协议开源。

- GitHub
  地址：[https://github.com/lightweight-component/aj-security](https://github.com/lightweight-component/aj-security)
- GitCode
  地址（适合中国用户快速访问）：[https://gitcode.com/lightweight-component/aj-security](https://gitcode.com/lightweight-component/aj-security)

## 相关链接

[用户手册](https://security.ajaxjs.com) | [JavaDoc](https://javadoc.io/doc/com.ajaxjs/aj-security) | [DeepWiki 页面](https://deepwiki.com/lightweight-component/aj-security)
