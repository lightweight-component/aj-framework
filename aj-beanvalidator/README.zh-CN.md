# AJ Bean Validator

一个不依赖 Hibernate Validator 的轻量 Spring MVC 校验扩展库，提供中国身份证、中国大陆手机号、用户名、密码、中文文本、IPv4 和 HTTP URL 等常用业务校验。

[English](README.md) | [繁體中文](README.zh-TW.md)

## 环境要求

- Java 8+
- Spring Boot 2.7+

本库 JAR 以 Java 8 字节码编译，支持 Spring Boot 2.7、3 和 4。Spring Boot 2.7 应用可运行在 Java 8+；Spring Boot 3 和 4 应用自身需要 Java 17+。

## 安装

在应用中加入依赖：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>beanvalidator</artifactId>
    <version>1.0</version>
</dependency>
```

## Spring Boot 集成

此 Starter 支持 Spring Boot 2.7+。在 Servlet 式 Spring MVC 应用中，加入依赖后会自动启用，无需手动配置。

如需暂时关闭校验器：

```yaml
ajaxjs:
  beanvalidator:
    enabled: false
```

## 自定义错误消息

自定义约束默认使用内置中文消息。可以通过标准 Spring Boot 配置覆盖；Profile、环境变量和命令行参数的优先级遵循 Spring Boot 规则。

```yaml
ajaxjs:
  beanvalidator:
    messages:
      id-card: 身份证号格式不正确
      mobile-no: 手机号格式不正确
```

注解上显式指定的 `message` 优先级最高：

```java
@IdCard(message = "请输入合法的身份证号")
private String idCard;
```

## 校验范围

本库不依赖 `javax.validation`、`jakarta.validation` 或 Hibernate Validator。下列全部是本库自己的注解，请从 `com.ajaxjs.framework.validator.custom` 导入。

| 注解 | 支持的值 | `null` 处理 |
| --- | --- | --- |
| `@NotNull` | 任意值 | 校验失败 |
| `@NotBlank` | `CharSequence` | 校验失败 |
| `@Size` | `CharSequence`、`Collection`、`Map`、数组 | 校验通过 |
| `@Min`、`@Max` | `Number` | 校验通过 |
| `@Pattern` | `CharSequence` | 校验通过 |
| `@Email` | `CharSequence` | 校验通过 |

`@Pattern` 和 `@Email` 支持自身的 `regexp` 和 `flags` 属性，`flags` 使用本库的 `Pattern.Flag` 枚举。这是一组轻量注解，不是 Bean Validation 实现。

```java
import com.ajaxjs.framework.validator.custom.Email;
import com.ajaxjs.framework.validator.custom.NotBlank;
import com.ajaxjs.framework.validator.custom.NotNull;
import com.ajaxjs.framework.validator.custom.Size;

class ContactRequest {
    @NotNull(message = "ID 必填")
    private Long id;

    @NotBlank(message = "姓名必填")
    @Size(max = 50, message = "姓名不能超过 50 个字符")
    private String name;

    @Email(message = "Email 格式不正确")
    private String email;
}
```

## 业务约束

自定义注解可用于请求对象字段和 Controller 方法参数。

| 注解 | 合法值 |
| --- | --- |
| `@IdCard` | 15 或 18 位中国身份证号；出生日期须合法，18 位还需通过校验码验证。 |
| `@MobileNo` | `1[3-9]xxxxxxxxx` 格式的中国大陆手机号。 |
| `@Username` | 以英文字母开头，总长度为 6–21 个单词字符。 |
| `@Password` | 8–16 个非空白字符，英文字母、数字、符号中至少包含两类。 |
| `@Chinese` | 一个或多个中文字符。 |
| `@Ipv4` | 完整的 IPv4 地址。 |
| `@HttpUrl` | 含主机名的 `http` 或 `https` URL，且不允许未转义的空格。 |

## 应用自定义约束

应用可以在自己的包中定义注解。实现一个 Spring `ValidatorRule` Bean 后，Starter 会自动将已注册规则应用于请求对象字段和路径变量。

```java
package com.example.validation;

import com.ajaxjs.framework.validator.ValidatorException;
import com.ajaxjs.framework.validator.ValidatorRule;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantCode {
    String message() default "租户编码不正确";
}

@Component
class TenantCodeRule implements ValidatorRule {
    public boolean supports(Annotation annotation) {
        return annotation.annotationType() == TenantCode.class;
    }

    public void validate(Annotation annotation, Object value, String fieldName) {
        TenantCode tenantCode = (TenantCode) annotation;
        if (!(value instanceof String) || !((String) value).matches("TENANT-\\d+"))
            throw new ValidatorException(fieldName + " " + tenantCode.message());
    }
}
```

请求对象示例：

```java
import com.ajaxjs.framework.validator.custom.Chinese;
import com.ajaxjs.framework.validator.custom.HttpUrl;
import com.ajaxjs.framework.validator.custom.IdCard;
import com.ajaxjs.framework.validator.custom.Ipv4;
import com.ajaxjs.framework.validator.custom.MobileNo;
import com.ajaxjs.framework.validator.custom.Password;
import com.ajaxjs.framework.validator.custom.Username;

class RegistrationRequest {
    @Username
    private String username;

    @Password
    private String password;

    @Chinese
    private String realName;

    @MobileNo
    private String mobile;

    @IdCard(required = false)
    private String idCard;

    @Ipv4(required = false)
    private String trustedIp;

    @HttpUrl(required = false)
    private String callbackUrl;

    // getters and setters
}
```

### `required`

每个自定义注解都有 `required` 属性，默认为 `true`。

- `required = true`：`null`、空字符串或全空白字符串都会校验失败。
- `required = false`：`null`、空字符串或全空白字符串会跳过该自定义格式校验。
- 只要值非空，就一定会校验其格式，不受 `required` 值影响。

`required` 只适用于上表中的业务约束。通用的必填语义请使用本库的 `@NotNull` 或 `@NotBlank`。

## Controller 用法

请求体使用 Spring 的 `@Validated`：

```java
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RegistrationController {
    @PostMapping("/registrations")
    void create(@Validated @RequestBody RegistrationRequest request) {
    }
}
```

路径变量上受支持的注解会自动执行：

```java
import com.ajaxjs.framework.validator.custom.IdCard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@GetMapping("/people/{idCard}")
String find(@PathVariable("idCard") @IdCard String idCard) {
    return idCard;
}
```

## 错误响应示例

请求体的自定义约束失败会收集到 Spring 的 `BindingResult` / `Errors`。可以使用 Controller Advice 返回 HTTP 400：

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
class ValidationErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> handleBodyValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new java.util.LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors())
            errors.put(error.getField(), error.getDefaultMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
```

不合法的路径变量会抛出 `ValidatorException`，应在应用的异常处理器中映射为 HTTP 400。`ValidatorConfigurationException` 表示开发配置错误，例如使用了不支持的自定义注解或配置了空白消息，通常应作为服务端错误处理。
