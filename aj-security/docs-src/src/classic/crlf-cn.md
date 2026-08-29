---
title: 防止 CRLF 攻击
subTitle: 2024-12-05 by Frank Cheung
description: 防止 CRLF 攻击、防止 Cookie 注入攻击
date: 2022-01-05
tags:
  - 防止 CRLF 攻击
  - 防止 Cookie 注入攻击
layout: layouts/docs-cn.njk
---

# 防止 CRLF 攻击

CRLF（Carriage Return Line Feed）攻击 是一种在 Web 应用程序中利用 HTTP
头部注入漏洞的攻击方式。它主要通过插入特殊字符`（\r\n）`来操控 HTTP 响应头。攻击者通过注入 CRLF，可以终止现有的 HTTP
头部并插入新的头部字段，或者直接构造新的 HTTP 响应。

防御 CRLF 攻击一般是对用户输入进行严格验证和过滤，禁止`\r`和`\n`。

## 配置

首先确定过滤器打开，即`enabled: true`，然后配置`crlfCheck`打开检测。

```yaml
security:
    web: # 常规攻击
        enabled: true
        crlfCheck: true # 防止 CRLF 攻击
```

# 防止 Cookie 注入攻击

在某些情况下，攻击者可能通过注入数据向 Cookie 中写入超大量信息，试图污染应用的状态或引发系统异常。 检查 Cookie 大小可以限制
Cookie 内容的容量，减少攻击者通过注入大量数据进行攻击的可能性。

## 配置

首先确定过滤器打开，即`enabled: true`，然后配置`cookiesSizeCheck`打开检测，最后设置`maxCookieSize`为容量大小。

```yaml
security:
    web: # 常规攻击
        enabled: true
        cookiesSizeCheck: true # 防止 Cookie 注入攻击
        maxCookieSize: 1 # 单个 cookie 最大大小，单位：kb
```