<h1 align="center">AJ Bean Validator</h1>
<h3 align="center">羽量级的 Java Bean 实体校验器</h3>

<div align="center" style="text-align: center;">


[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.3.7-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util )
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-util)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![中文](https://img.shields.io/badge/lang-中文-red)](./README.zh-CN.md)

</div>

<hr />

A lightweight Spring MVC validation extension for common application-specific values, without a Hibernate Validator
dependency. It provides validators for Chinese identity cards, mainland mobile numbers, usernames, passwords, Chinese
text, IPv4 addresses, and HTTP URLs.

[简体中文](README.zh-CN.md) | [繁體中文](README.zh-TW.md)

## Requirements

- Java 8+
- Spring Boot 2.7+

The library JAR is compiled for Java 8 and supports Spring Boot 2.7, 3, and 4. Spring Boot 2.7 applications may run on
Java 8+; Spring Boot 3 and 4 applications themselves require Java 17+.

## Installation

Add the library to the application that consumes it:

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>beanvalidator</artifactId>
    <version>1.0</version>
</dependency>
```

## Spring Boot integration

This starter supports Spring Boot 2.7+. In a servlet-based Spring MVC application, it is enabled automatically after
adding the dependency; no manual configuration is required.

Disable it when necessary:

```yaml
ajaxjs:
  beanvalidator:
    enabled: false
```

## Custom messages

Custom constraints use built-in Chinese messages by default. Override them through normal Spring Boot configuration;
profiles, environment variables, and command-line properties follow Spring Boot's usual precedence rules.

```yaml
ajaxjs:
  beanvalidator:
    messages:
      id-card: Invalid ID card number
      mobile-no: Invalid mobile number
```

An explicit `message` on an annotation still takes precedence:

```java
@IdCard(message = "Please provide a valid ID card number")
private String idCard;
```

## Validation scope

The library does not depend on `javax.validation`, `jakarta.validation`, or Hibernate Validator. The following are this
library's own annotations; import them from `com.ajaxjs.framework.validator.custom`.

| Annotation     | Supported values                            | `null` handling |
|----------------|---------------------------------------------|-----------------|
| `@NotNull`     | Any value                                   | Fails           |
| `@NotBlank`    | `CharSequence`                              | Fails           |
| `@Size`        | `CharSequence`, `Collection`, `Map`, arrays | Valid           |
| `@Min`, `@Max` | `Number`                                    | Valid           |
| `@Pattern`     | `CharSequence`                              | Valid           |
| `@Email`       | `CharSequence`                              | Valid           |

`@Pattern` and `@Email` support their `regexp` and `flags` attributes. `flags` uses this library's `Pattern.Flag` enum.
This is a lightweight annotation set, not a Bean Validation implementation.

```java
import com.ajaxjs.framework.validator.custom.Email;
import com.ajaxjs.framework.validator.custom.NotBlank;
import com.ajaxjs.framework.validator.custom.NotNull;
import com.ajaxjs.framework.validator.custom.Size;

class ContactRequest {
    @NotNull(message = "ID is required")
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @Email(message = "Invalid email")
    private String email;
}
```

## Business constraints

Custom constraints are supported on request-object fields and controller method parameters.

| Annotation  | Valid value                                                                                                         |
|-------------|---------------------------------------------------------------------------------------------------------------------|
| `@IdCard`   | A 15- or 18-digit Chinese ID card number with a valid birth date; 18-digit values also require a valid check digit. |
| `@MobileNo` | Mainland mobile number in the `1[3-9]xxxxxxxxx` format.                                                             |
| `@Username` | Starts with a letter and is 6–21 word characters long.                                                              |
| `@Password` | 8–16 non-whitespace characters containing at least two of letters, digits, and symbols.                             |
| `@Chinese`  | One or more Chinese characters.                                                                                     |
| `@Ipv4`     | A complete IPv4 address.                                                                                            |
| `@HttpUrl`  | An `http` or `https` URL with a hostname and no unescaped spaces.                                                   |

## Application-defined constraints

Applications can define annotations in their own packages. Add a Spring bean implementing `ValidatorRule`; the starter
automatically applies registered rules to request-object fields and path variables.

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
    String message() default "Invalid tenant code";
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

Example request object:

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

Each custom annotation has `required`, which defaults to `true`.

- `required = true`: `null`, an empty string, or whitespace-only text fails validation.
- `required = false`: `null`, an empty string, or whitespace-only text skips this custom format validation.
- A non-empty value is always validated, regardless of `required`.

`required` applies only to the business constraints in the table above. Use this library's `@NotNull` or `@NotBlank` for
general required-field semantics.

## Controller usage

Use Spring's `@Validated` for request-body objects:

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

Supported annotations on path variables are applied automatically:

```java
import com.ajaxjs.framework.validator.custom.IdCard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@GetMapping("/people/{idCard}")
String find(@PathVariable("idCard") @IdCard String idCard){
        return idCard;
        }
```

## Error handling example

For request-body validation, custom constraint failures are collected in Spring's `BindingResult` / `Errors`. A
controller advice can return a 400 response such as:

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

Invalid path variables throw `ValidatorException`; map it to a 400 response in the application's exception
handler. `ValidatorConfigurationException` indicates a developer configuration error, such as an unsupported custom
annotation or a blank message, and should normally be reported as a server-side error.
