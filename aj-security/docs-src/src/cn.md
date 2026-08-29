---
title: AJ Security 首页
subTitle: 2024-12-05 by Frank Cheung
description: AJ Security 简介
date: 2022-01-05
tags:
  - AJ Security 简介
layout: layouts/docs-cn.njk
---

# AJ Security：实用的 Java Web 安全库

基于 Spring/HandlerInterceptor 拦截器机制，抽象一套过滤/校验的机制，形成统一的一套调用链，可灵活配置并扩展。本安全框架架构简单，代码精炼，没有其他额外的依赖，适用于任何基于
Spring 的项目。Spring Boot 程序引入 jar 包即可开箱即用。

本框架的功能有：

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

</td>

<td>

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

</td></tr></table>

AJ Security 的代码量不多（JAR 约 100kb 大小），而且都是很简单易懂的代码。另外没啥第三方依赖，适合 Java8+。

## 源代码

本项目采用 Apache License 协议开源。

- GitHub
  地址：[https://github.com/lightweight-component/aj-security](https://github.com/lightweight-component/aj-security)
- GitCode
  地址（适合中国用户快速访问）：[https://gitcode.com/lightweight-component/aj-security](https://gitcode.com/lightweight-component/aj-security)

## 相关链接

[用户手册](https://security.ajaxjs.com) | [JavaDoc](https://javadoc.io/doc/com.ajaxjs/aj-security) | [DeepWiki 页面](https://deepwiki.com/lightweight-component/aj-security)

