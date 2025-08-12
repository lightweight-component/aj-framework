---
title: 安全组件
subTitle: 2024-12-05 by Frank Cheung
description: 安全组件
date: 2022-01-05
tags:
  - 安全组件
layout: layouts/aj-docs.njk
---
# 用户系统

用户管理、单点登录、权限管理，请参阅独立的组件 AJ-IAM：[https://iam.ajaxjs.com](https://iam.ajaxjs.com)。

# 安全组件

请参阅独立的组件：aj-security：[https://security.ajaxjs.com](https://security.ajaxjs.com)。


基于 Spring/HandlerInterceptor 拦截器机制，抽象一套过滤/校验的机制，形成统一的一套调用链，可灵活配置并扩展。本安全框架架构简单，代码精炼，没有其他额外的依赖，适用于任何基于 Spring 的项目。Spring Boot 程序引入 jar 包即可开箱即用。


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
    - 限流限次数
- 其他实用功能
    - 实体字段脱敏
    - API 接口加解密

