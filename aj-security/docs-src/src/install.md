---
title: Installation and Configuration
subTitle: 2024-12-05 by Frank Cheung
description: Installation and Configuration
date: 2022-01-05
tags:
  - Installation
  - Configuration
layout: layouts/docs.njk
---

# Installation

This component integrates seamlessly with Spring Boot applications. After adding the dependency, you can use it in your
Spring Boot project. Maven coordinates:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-security</artifactId>
    <version>1.4</version>
</dependency>
```

It has been tested on Java 8 + SpringBoot 2.7.

Currently, non-Spring applications are not supported. If you want to use it in a traditional Spring MVC application, you
may need to modify related configuration mechanisms accordingly.

## Demo Application

We provide a [Sample Demo](https://gitcode.com/lightweight-component/aj-security/tree/main/aj-security-samples) in the
source code for installation and configuration reference. It also serves as part of the test cases and includes
demonstrations of all security components. Browse the source code in the `aj-security-samples` directory; this is a
standard SpringBoot v2.7 project.  
To start the demo, simply run the `main()` function of the `FooApp` class.

# Configuration

Developers mainly interact with AJ Security through configuration files. Add the following content to the Spring
configuration YAML file `application.yml`:

```
security:
  HttpReferer: # Referer interception
    globalCheck: false # Global check
    enabled: true
    allowedReferrers:
      - https://example.com
      - https://another-example.com
      - https://my-site.com

  TimeSignature: # Timestamp control
    enabled: true
    secretKey: der3@x7Az#2 # Secret key, required

  IpList: # IP whitelist/blacklist
    globalCheck: false # Global check
    enabled: true
    whiteList:
      - 192.168.1.1
      - 192.168.1.2

  HttpBasicAuth: # HTTP Basic Authentication
    globalCheck: true # Global check
    enabled: false
    username: admin
    password: admin

  HttpDigestAuth: # HTTP Digest Authentication
    globalCheck: true # Global check
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

You can see that there are configuration options for each security component under `security`.  
Abstractly, there are two common configuration options for each component: `enabled` and `globalCheck`.

- `enabled`: Whether to activate this component. If set to `false`, the component will not run. We have agreed that as
  long as a component's `enabled` property is set to true, the component will be activated; otherwise, it will not run (
  and related objects will not even be created, in order to save resources).
- `globalCheck`: Whether to perform a global check. If set to `true`, all requests will be checked by this component;
  if `false`, only requests matching specific URLs will be checked. The matching method is declared via annotations on
  Spring controllers, for example:

```java
@GetMapping("/HttpRefererCheck")
@HttpRefererCheck
int HttpRefererCheck();
```

For detailed configuration of each component, please refer to the respective chapters.

## Basic Detail

We provide more than ten different security components, all of which are deeply integrated with the Spring framework. We
make full use of Spring's extensible architecture and flexible configuration mechanisms.

Most components are implemented using `HandlerInterceptor`, while some special components
utilize `HandlerMethodArgumentResolver`, traditional Servlet `Filter`, or `HttpMessageConverter`.