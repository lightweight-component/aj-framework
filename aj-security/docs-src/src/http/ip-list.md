---
title: IP Whitelist/Blacklist
subTitle: 2024-12-05 by Frank Cheung
description: MCP Server SDK Prompts Development
date: 2022-01-05
tags:
  - IP Whitelist/Blacklist
layout: layouts/docs.njk
---

# IP Whitelist/Blacklist

IP whitelist/blacklist validation is a common security measure used to allow or deny specific IP addresses access to
interfaces, pages, or services.

## Scenario Description

- Whitelist: Only IPs on the whitelist are allowed access; all others are denied.
- Blacklist: IPs on the blacklist are denied access; all others are allowed.

# Usage

## YAML Configuration

Usually, only one of whitelist or blacklist is set at a time.

```yaml
security:
  IpList:
    globalCheck: true # Global check
    enabled: true
    whiteList:
      - 192.168.1.1
      - 192.168.1.2
```

## Interceptor Validation

Add the `@IpListCheck` annotation to the interface in use:

```java
@GetMapping("/IpListCheck")
@IpListCheck
int IpListCheck();
```

# Roadmap

Consider IPv6 and separate configuration for annotations.