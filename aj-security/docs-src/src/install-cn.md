---
title: 安装与配置
subTitle: 2024-12-05 by Frank Cheung
description: 安装与配置
date: 2022-01-05
tags:
  - 安装
  - 配置
layout: layouts/docs-cn.njk
---

# 安装

本组件与 Spring Boot 程序无缝衔接。添加依赖后，即可在 Spring Boot 项目中使用。Maven 坐标：

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-security</artifactId>
    <version>1.3/version>
</dependency>
```

在 Java 8 + SpringBoot2.7 通过测试。

当前不支持非 Spring 程序。如果要在传统 Spring MVC 程序中使用，可能要针对配置的相关机制进行修改。

## 演示程序

我们于源码中附带有[演示程序 Sample](https://gitcode.com/lightweight-component/aj-security/tree/main/aj-security-samples)
可供安装配置参考，同时也作为测试用例的一部分，包含了各个安全组件的演示。浏览源码`aj-security-samples`目录，这是一个标准的
SpringBoot v2.7 工程。
执行`FooApp`类的`main()`函数即可启动该演示程序。

# 配置

开发者与 AJ Security 打交道更多是通过配置文件进行配置的。在 Spring 配置 YAML 文件`application.yml`中添加如下内容：

```
security:
  HttpReferer: # Referer 拦截
    globalCheck: false # 全局检查
    enabled: true
    allowedReferrers:
      - https://example.com
      - https://another-example.com
      - https://my-site.com

  TimeSignature: # 时间戳控制
    enabled: true
    secretKey: der3@x7Az#2 # 密钥，必填的

  IpList: # ip 白名单/黑名单
    globalCheck: false # 全局检查
    enabled: true
    whiteList:
      - 192.168.1.1
      - 192.168.1.2

  HttpBasicAuth: # HTTP Basic 认证
    globalCheck: true # 全局检查
    enabled: false
    username: admin
    password: admin

  HttpDigestAuth: # HTTP Digest 认证
    globalCheck: true # 全局检查
    enabled: false
    username: admin
    password: admin2
  EncryptedBody:
    publicKey: MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmkKluNutOWGmAK2U80hM9JtzsgLAlgvHqncakqwhruE9TIXUQFDRKsIBQwN+3rLC76kyOl4U+eBefLaGQGJBZVq0qwIHBe4kfH0eJXaHyG/i9H2Iph1cyY6cn6ocPta6ZmSuOIcx4yLlpCgq5eDRigHs0AR418ZTlRItlhrY9+wIDAQAB
    privateKey: MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKaQqW42605YaYArZTzSEz0m3OyAsCWC8eqdxqSrCGu4T1MhdRAUNEqwgFDA37essLvqTI6XhT54F58toZAYkFlWrSrAgcF7iR8fR4ldofIb+L0fYimHVzJjpyfqhw+1rpmZK44hzHjIuWkKCrl4NGKAezQBHjXxlOVEi2WGtj37AgMBAAECgYBj9sX4o3UtG9qVVXX4votVVBGaztDocmIF0JL7GLqBC6hv19CNydJoUO1xiY+6iCW5YbB4k28gQqrKmXQxKszWFdd1NTHOKS3nti8I2QNc4T9FF34YvYh/WQlRw7dHmYUl/MCm6U6yVE7XK8GoYYOyAyclXuFR+SRw8/gHsoAYoQJBAPQ3L+K47QSujMSzu8ZcdRingN25VS8r790A18WNxtK9l/7b3l8aTUXmeGcjLpQDnx158jQ32fTUki5aa2eGDp0CQQCumkCpTgcvx0Ys66aXKnpaexWGWDK/ui9hY7lRdd2XijK30Uo2TlQ1ujXYjodbJJUAELE3UAC+0yj8W8Edf093AkAzrHWyaGSmZ/SbLlieCTQxqkenIq72kzpmreX6BBy8vKcrowQzZVJSZwi08gnKAdYqG4J3MBYrKstfiXxOZFw1AkA6+3radrRwzHWFWTnWmQ/qHug/kO3b3M6CrMh+nz1zIslNVVMnk0BZQgVMmaFaBbqb4gerssf9rqGK1ogfKdGzAkBQzGcbSAtTlCAMNMnOXphIvFQ/GnxlPwwr23ysyt0k14SOwGNfND4rXM2rTzjz+2yF20tGdGgmXfwnvKOaCc1N
    enabled: true
  ParamsSign:
    enabled: true
    secretKey: der3@x7Az42
```

可见在`security`中有各个安全组件的配置。抽象来看，主要有个配置是各个组件共用的，分别是`enabled`与`globalCheck`。

- `enabled`是否激活该组件。若为`false`则不运行该组件。我们约定，只要某组件的`enabled`为`true`
  ，则该组件会运行，否则不运行（而且不创建相关对象，以节省资源）
- `globalCheck`是否全局检查。若为`true`则所有请求都会检查该组件，若为`false`则只有匹配 URL 的请求才会检查该组件。匹配的方式通过在
  Spring 控制器上的注解声明。例如：

```java
@GetMapping("/HttpRefererCheck")
@HttpRefererCheck
int HttpRefererCheck();
```

各个组件的详细配置方式请参考其各个章节。

## 基本原理

我们提供了近十多种安全组件，几乎都是与 Spring 框架深度绑定，充分利用了 Spring 的可扩展机制与灵活的配置机制。
多数组件通过`HandlerInterceptor`拦截器实现，其他特殊组件采用`HandlerMethodArgumentResolver`
、传统的`Servlet Filter`、`HttpMessageConverter`均有。