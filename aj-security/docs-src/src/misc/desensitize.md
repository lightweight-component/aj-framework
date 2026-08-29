---
title: Entity Field Desensitization
subTitle: 2024-12-05 by Frank Cheung
description: Entity Field Desensitization
date: 2022-01-05
tags:
  - Desensitization
layout: layouts/docs.njk
---

# Entity Field Desensitization

Desensitization refers to partially obscuring sensitive fields, ensuring data is not fully exposed while retaining
enough information for identification. Common fields requiring desensitization include names, phone numbers, emails,
usernames, passwords, etc.

Since Java entities are typically either Java Beans or Maps, desensitization can be applied directly to entity data. It
is crucial to control where the desensitization component is invoked, such as processing entities before returning them
in REST APIs. However, RPC scenarios may require different handling.

The implementation of desensitization is straightforward, essentially involving simple string replacement. However,
additional considerations are needed to address various scenarios involving entity fields.

## Source Code

This component's source code is forked
from [emily-project](https://github.com/mingyang66/spring-parent/tree/master/emily-project/emily-desensitize). Special
thanks to the original author!

# Usage

## Define Entity Annotations

```java
import com.ajaxjs.security.desensitize.DesensitizeType;
import com.ajaxjs.security.desensitize.annotation.DesensitizeModel;
import com.ajaxjs.security.desensitize.annotation.DesensitizeProperty;
import lombok.Data;

@Data
@DesensitizeModel
public class User {
    private String name;

    @DesensitizeProperty(DesensitizeType.PHONE)
    private String phone;

    private int age;
}
```

In the example above:

- `@DesensitizeModel` indicates that the POJO should be desensitized.
- `@DesensitizeProperty(DesensitizeType.PHONE)` specifies the field to be desensitized and its type (e.g., "phone").
  Other types can be found in the `DesensitizeType` enum:

```java
/**
 * Desensitization Types
 */
public enum DesensitizeType {
    DEFAULT(v -> DataMask.PLACE_HOLDER),
    // Phone number
    PHONE(DataMask::maskPhoneNumber),
    // Bank card number
    BANK_CARD(DataMask::maskBankCard),
    // ID card number
    ID_CARD(DataMask::maskIdCard),
    // Name
    USERNAME(DataMask::maskChineseName),
    // Email
    EMAIL(DataMask::maskEmail),
    // Address
    ADDRESS(v -> DataMask.maskAddress(v, 0));

    public final Function<String, String> handler;

    DesensitizeType(Function<String, String> handler) {
        this.handler = handler;
    }
}
```

Manual desensitization can be performed using: `DeSensitize.acquire(body);`.

## Define Controller Annotations

Use `@Desensitize` to annotate controller methods.

```java
@GetMapping("/user_desensitize")
@Desensitize
public User UserDesensitize() {
    User user = new User();
    user.setAge(1);
    user.setName("tom");
    user.setPhone("13711118120");

    return user;
}
```

The way to integrate the data desensitization component is somewhat special — it's not done through conventional
extension points. Essentially, all we need to do is return an entity result, so we can simply modify (i.e., desensitize)
the data at the point of final entity output.

In this approach, each system may have a different location where uniform response objects are handled. In the current
example, responses are unified using `ResponseBodyAdvice`, and only a single line of logic needs to be added for
desensitization.

As for this kind of centralized response handling — most Spring applications already have something like it. If your
project doesn't, you’ll need to consider alternative approaches accordingly.

```java
import com.ajaxjs.security.desensitize.DeSensitize;
import com.ajaxjs.security.desensitize.annotation.Desensitize;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;

@RestControllerAdvice
@Component
public class GlobalResponseResult implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Method method = returnType.getMethod();
        assert method != null;

        if (method.isAnnotationPresent(Desensitize.class)) // Check if desensitization is required
            body = DeSensitize.acquire(body);

        ResponseResultWrapper responseResult = new ResponseResultWrapper();
        responseResult.setStatus(1);
        responseResult.setData(body);

        return responseResult;
    }
}
```

Example response:

```json
{
    "status": 1,
    "errorCode": null,
    "message": "Operation successful",
    "data": {
        "phone": "137*****8120",
        "name": "tom",
        "age": 1
    }
}
```

# Class Descriptions

- **DeSensitizeUtils**: This class performs desensitization directly on the original entity object, modifying its
  fields.
- **SensitizeUtils**: This class creates a new object instance (or collection) and applies desensitization rules to the
  new object, leaving the original object unchanged. The returned object has the same structure but with values
  desensitized.