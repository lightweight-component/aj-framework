---
title: Contact Us
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - timestamp
layout: layouts/docs-cn.njk
---

# 时间戳请求校验

时间戳（Timestamp）请求校验主要用于防止请求重放（Replay
Attack），原理是在每次请求中附带一个时间戳（通常是毫秒），后端校验该时间戳是否在允许的时间窗口内。常与签名机制配合，确保请求的时效性和唯一性，但也可以单独使用。

使用场景：重置密码时候，发送 URL 地址的时候须附带这个时间戳密文转为参数；对 API
保密比较高的，且服务端调用的（不能是浏览器调用的，因为密钥在浏览器处保存的话，不安全）。

## 常见校验逻辑

1. 客户端请求时带上时间戳（如参数`timestamp=xxx`）密文。这个时间戳密文由密钥生成，由服务端或者客户端保存（注意不能泄露）
1. 后端校验当前服务器时间与请求时间戳之差，在合理范围内（如±5分钟）。
1. 超时则拒绝请求。

重放攻击：仅时间戳校验无法防止同一请求多次提交，需配合唯一`nonce`，对于带有业务幂等性要求的接口，也校验`nonce`是否已用过。

# 使用方式

## YAML 配置

当前采用 AES 对称加密。添加你的 AES 密钥。

```yaml
security:
    TimeSignature: # 时间戳控制
        enabled: true
        secretKey: der3@x7Az#2 # 密钥，必填的
```

## 拦截校验

在使用的接口上添加`@TimeSignatureVerify`注解：

```java
@GetMapping("/TimeSignatureVerify")
@TimeSignatureVerify
int TimeSignatureVerify();
```

## 生成时间戳密文 token

作为参数分发到你的需求代码中去。

```java
// 静态方法可调用
String token=SecurityInterceptor.getBean(TimeSignature.class).generateSignature();
```

# Roadmap

更复杂的加密规则、针对注解的单独配置