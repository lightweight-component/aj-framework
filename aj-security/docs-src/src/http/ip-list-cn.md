---
title: IP 白名单/黑名单
subTitle: 2024-12-05 by Frank Cheung
description: MCP Server SDK Prompts Development
date: 2022-01-05
tags:
  - IP 白名单/黑名单
layout: layouts/docs-cn.njk
---

# IP 白名单/黑名单

IP 白名单/黑名单校验是一种常见的安全措施，用于允许或拒绝特定IP地址访问接口、页面或服务。

## 场景说明

- 白名单：只有在白名单内的IP才能访问，其它全部拒绝。
- 黑名单：黑名单内的IP拒绝访问，其它全部允许。

# 使用方式

## YAML 配置

白名单、黑名单同时一般只设置一种。

```yaml
security:
  IpList:
    globalCheck: true # 全局检查
    enabled: true
    whiteList:
      - 192.168.1.1
      - 192.168.1.2
```

## 拦截校验

在使用的接口上添加`@IpListCheck`注解：

```java
@GetMapping("/IpListCheck")
@IpListCheck
int IpListCheck();
```

# Roadmap

考虑 ipv6、针对注解的单独配置