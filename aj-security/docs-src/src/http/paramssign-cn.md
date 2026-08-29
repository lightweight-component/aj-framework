---
title: 参数签名校验
subTitle: 2024-12-05 by Frank Cheung
description: 参数签名校验（Parameter Signature Verification）用于防止参数被篡改、重放攻击、伪造请求等。其基本原理是：将请求参数与密钥通过特定算法（常见如MD5、SHA256、HMAC等）计算出签名，服务端用同样逻辑验证签名是否一致
date: 2022-01-05
tags:
  - 参数签名
layout: layouts/docs-cn.njk
---

# 参数签名校验

参数签名校验（Parameter Signature
Verification）用于防止参数被篡改、重放攻击、伪造请求等。其基本原理是：将请求参数与密钥通过特定算法（常见如MD5、SHA256、HMAC等）计算出签名，服务端用同样逻辑验证签名是否一致。

## 典型流程

### 前端/客户端

- 组装请求参数，按规定排序（通常是字典序）。
- 拼接密钥（如 app_secret）。
- 按算法生成签名（sign）。
- 将所有参数和 sign 一起提交给后端。

### 后端

- 拿到请求参数，去掉 sign。
- 按同样规则排序拼接，加密生成自己 的sign。
- 比较客户端传来的 sign 和自己生成的 sign 是否一致。
- 一致则通过，否则拒绝。

## 参考

- https://mp.weixin.qq.com/s/Ij87Ut2doyn5xVGcxFZvuA