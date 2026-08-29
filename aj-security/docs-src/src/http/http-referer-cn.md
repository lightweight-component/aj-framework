---
title: HTTP Referer 校验
subTitle: 2024-12-05 by Frank Cheung
description: HTTP Referer 校验（也称为“Referer 检查”）是一种常见的 Web 安全措施，其原理是：后端服务器在接收请求时检查请求头中的 Referer 字段，判断请求来源是否为信任的域名或页面。
date: 2022-01-05
tags:
  - Referer
layout: layouts/docs-cn.njk
---

# HTTP Referer 校验

HTTP Referer 校验（也称为“Referer 检查”）是一种常见的 Web 安全措施，其原理是：后端服务器在接收请求时检查请求头中的 Referer
字段，判断请求来源是否为信任的域名或页面。

## 基本原理

1. 客户端（浏览器）在发起 HTTP 请求时，会在请求头里带上`Referer`，指明请求的来源页面地址。
1. 服务器端在收到请求后，读取`Referer`，判断其是否为受信任的来源。
1. 若`Referer`不符合要求，则拒绝请求或返回错误。

## 常用场景

- 表单提交、接口调用等敏感操作的来源校验
- 防止 CSRF 攻击
- 防盗链（如图片、视频资源只允许本域名访问）

## 注意事项

- Referer 不是所有请求都带（如直接输入网址、某些浏览器隐私模式、HTTPS 到 HTTP）
- Referer 容易被伪造，不能作为唯一安全手段，只能作为补充
- 建议结合 CSRF Token、Cookie 验证等多重措施

# 使用方式

## yaml 配置

```yaml
security:
  HttpReferer: # Referer 拦截
    globalCheck: false # 全局检查
    enabled: true
    allowedReferrers:
      - https://example.com
      - https://another-example.com
      - https://my-site.com
```

## 拦截校验

在使用的接口上添加`@HttpRefererCheck`注解：

```java
@GetMapping("/HttpRefererCheck")
@HttpRefererCheck
int HttpRefererCheck();
```
