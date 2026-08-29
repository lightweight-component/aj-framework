---
title: Prevent CRLF Attacks
subTitle: 2024-12-05 by Frank Cheung
description: Prevent CRLF Attacks/Prevent Cookie Injection Attacks
date: 2022-01-05
tags:
  - Prevent CRLF Attacks
  - Prevent Cookie Injection Attacks
layout: layouts/docs.njk
---

# Prevent CRLF Attacks

CRLF (Carriage Return Line Feed) attacks are a type of vulnerability in web applications that exploit HTTP header
injection. By inserting special characters `(\r\n)`, attackers can manipulate HTTP response headers. Attackers use CRLF
injection to terminate existing HTTP headers and insert new header fields, or even construct new HTTP responses.

To prevent CRLF attacks, it's essential to strictly validate and filter user inputs, prohibiting `\r` and `\n`.

## Configuration

First, ensure the filter is enabled by setting `enabled: true`, and then enable `crlfCheck` to activate detection.

```yaml
security:
    web: # General attack prevention
        enabled: true
        crlfCheck: true # Prevent CRLF attacks
```

# Prevent Cookie Injection Attacks

In certain cases, attackers may attempt to inject excessive data into cookies to pollute the application's state or
cause system anomalies. Checking cookie size can limit the cookie content's capacity and reduce the likelihood of
attackers injecting large amounts of data.

## Configuration

First, ensure the filter is enabled by setting `enabled: true`, then enable `cookiesSizeCheck` to activate detection,
and finally set `maxCookieSize` to define the maximum cookie size.

```yaml
security:
    web: # General attack prevention
        enabled: true
        cookiesSizeCheck: true # Prevent Cookie Injection Attacks
        maxCookieSize: 1 # Maximum size of a single cookie, unit: KB
```