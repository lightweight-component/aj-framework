# AJ Bean Validator

一個不依賴 Hibernate Validator 的輕量 Spring MVC 驗證擴充套件，提供中國身分證、中國大陸手機號碼、使用者名稱、密碼、中文文字、IPv4
與 HTTP URL 等常用業務規則。

[English](README.md) | [简体中文](README.zh-CN.md)

## 環境需求

- Java 8+
- Spring Boot 2.7+

本套件 JAR 以 Java 8 位元碼編譯，支援 Spring Boot 2.7、3 與 4。Spring Boot 2.7 應用程式可執行於 Java 8+；Spring Boot 3 與 4
應用程式本身需要 Java 17+。

## 安裝

在使用此套件的應用程式中加入本套件：

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>beanvalidator</artifactId>
    <version>1.0</version>
</dependency>
```

## Spring Boot 整合

此 Starter 支援 Spring Boot 2.7+。在 Servlet 式 Spring MVC 應用程式中，加入相依後會自動啟用；不需要手動設定。

如有需要，可透過以下設定停用：

```yaml
ajaxjs:
  beanvalidator:
    enabled: false
```

## 自訂錯誤訊息

自訂約束預設使用內建中文訊息。可透過標準 Spring Boot 設定覆寫；Profile、環境變數與命令列參數的優先序會依照 Spring Boot 規則。

```yaml
ajaxjs:
  beanvalidator:
    messages:
      id-card: 身份證號格式不正確
      mobile-no: 手機號格式不正確
```

註解上明確寫出的 `message` 仍有最高優先序：

```java
@IdCard(message = "請輸入合法的身份證號")
private String idCard;
```

## 驗證範圍

本套件不依賴 `javax.validation`、`jakarta.validation` 或 Hibernate
Validator。下列全部是本套件自己的註解，請從 `com.ajaxjs.framework.validator.custom` 匯入。

| 註解            | 支援的值                                 | `null` 處理 |
|---------------|--------------------------------------|-----------|
| `@NotNull`    | 任何值                                  | 驗證失敗      |
| `@NotBlank`   | `CharSequence`                       | 驗證失敗      |
| `@Size`       | `CharSequence`、`Collection`、`Map`、陣列 | 驗證通過      |
| `@Min`、`@Max` | `Number`                             | 驗證通過      |
| `@Pattern`    | `CharSequence`                       | 驗證通過      |
| `@Email`      | `CharSequence`                       | 驗證通過      |

`@Pattern` 與 `@Email` 支援它們的 `regexp` 與 `flags` 屬性，`flags` 使用本套件的 `Pattern.Flag` 列舉。這是一組輕量註解，不是
Bean Validation 實作。

```java
import com.ajaxjs.framework.validator.custom.Email;
import com.ajaxjs.framework.validator.custom.NotBlank;
import com.ajaxjs.framework.validator.custom.NotNull;
import com.ajaxjs.framework.validator.custom.Size;

class ContactRequest {
    @NotNull(message = "身分識別碼必填")
    private Long id;

    @NotBlank(message = "名稱必填")
    @Size(max = 50, message = "名稱不可超過 50 字")
    private String name;

    @Email(message = "Email 格式不正確")
    private String email;
}
```

## 業務約束

自訂註解支援用於請求物件欄位與 Controller 方法參數。

| 註解          | 合法值                                      |
|-------------|------------------------------------------|
| `@IdCard`   | 15 或 18 位中國身分證號；須有合法出生日期，18 位另須有正確校驗碼。   |
| `@MobileNo` | 格式為 `1[3-9]xxxxxxxxx` 的中國大陸手機號碼。         |
| `@Username` | 以英文字母開始，總長度為 6–21 個單字字元。                 |
| `@Password` | 8–16 個非空白字元，英文字母、數字、符號至少包含其中兩類。          |
| `@Chinese`  | 一個或多個中文字元。                               |
| `@Ipv4`     | 完整 IPv4 位址。                              |
| `@HttpUrl`  | 有主機名稱、使用 `http` 或 `https`，且不含未跳脫空白的 URL。 |

## 應用程式自訂約束

應用程式可在自己的套件中定義註解。實作一個 Spring `ValidatorRule` Bean 後，Starter 會自動將已註冊規則套用至請求物件欄位與路徑變數。

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
    String message() default "租戶編碼不正確";
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

請求物件範例：

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

每個自訂註解都有 `required` 屬性，預設為 `true`。

- `required = true`：`null`、空字串與只包含空白的文字都會驗證失敗。
- `required = false`：`null`、空字串與只包含空白的文字會略過此自訂格式驗證。
- 只要值不是空的，就一定會進行格式驗證，不受 `required` 設定影響。

`required` 僅適用於上表中的業務約束。一般必填欄位請使用本套件的 `@NotNull` 或 `@NotBlank`。

## Controller 用法

Request body 請使用 Spring 的 `@Validated`：

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

路徑變數上受支援的註解會自動執行：

```java
import com.ajaxjs.framework.validator.custom.IdCard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@GetMapping("/people/{idCard}")
String find(@PathVariable("idCard") @IdCard String idCard){
        return idCard;
        }
```

## 錯誤回應範例

Request body 的自訂約束錯誤會收集到 Spring 的 `BindingResult` / `Errors`。可使用 Controller Advice 回傳 HTTP 400：

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

不合法的路徑變數會拋出 `ValidatorException`，應在應用程式的例外處理器中對應為 HTTP 400。`ValidatorConfigurationException`
表示開發設定錯誤，例如使用未支援的自訂註解或設定空白的訊息；通常應作為伺服器端錯誤處理。
