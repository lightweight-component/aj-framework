# AJ Bean Validator 入门教程

AJ Bean Validator 是一个轻量的 Spring MVC 校验 Starter。它的目标很简单：给请求对象或路径变量加上注解，在请求进入业务代码前完成基本格式检查。

它不依赖庞大的 Hibernate Validator，也不使用 `javax.validation` 或 `jakarta.validation` 的标准注解。使用时请只导入本库的注解包：

```java
import com.ajaxjs.framework.validator.custom.NotBlank;
```

本文从“注册一个用户”的接口开始，带你完成一次完整校验。

## 1. 适用环境

- Java 8+
- Spring Boot 2.7+
- Servlet 式 Spring MVC（即通常使用 `spring-boot-starter-web` 的项目）

本库自身按 Java 8 字节码编译。Boot 2.7 项目可以使用 Java 8+；Boot 3、4 的应用本身要求 Java 17+。

## 2. 加入依赖

在应用的 `pom.xml` 中加入：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>beanvalidator</artifactId>
    <version>1.0</version>
</dependency>
```

启动 Spring Boot 应用即可。这个依赖是 Starter，会自动把校验器接入 Spring MVC；通常不需要手工创建 `ValidatorImpl` 或 `ValidatorInitializing`。

如果某个环境暂时不想启用它，可以配置：

```yaml
ajaxjs:
  beanvalidator:
    enabled: false
```

## 3. 第一个请求对象

假设接口接收用户名、密码、姓名、手机号和邮箱：

```java
import com.ajaxjs.framework.validator.custom.Chinese;
import com.ajaxjs.framework.validator.custom.Email;
import com.ajaxjs.framework.validator.custom.MobileNo;
import com.ajaxjs.framework.validator.custom.NotBlank;
import com.ajaxjs.framework.validator.custom.Password;
import com.ajaxjs.framework.validator.custom.Size;
import com.ajaxjs.framework.validator.custom.Username;

class RegistrationRequest {
    @Username(message = "用户名格式不正确")
    private String username;

    @Password(message = "密码至少 8 位，且需包含两类字符")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Chinese(message = "姓名只能包含中文")
    @Size(max = 20, message = "姓名不能超过 20 个字符")
    private String realName;

    @MobileNo(message = "手机号格式不正确")
    private String mobile;

    @Email(message = "邮箱格式不正确")
    private String email;

    // 省略 getter 和 setter
}
```

然后在 Controller 参数上加 Spring 的 `@Validated`：

```java
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RegistrationController {
    @PostMapping("/registrations")
    void create(@Validated @RequestBody RegistrationRequest request) {
        // 进入这里时，request 已通过校验
    }
}
```

例如请求内容为：

```json
{
  "username": "ajaxjs",
  "password": "abc12345",
  "realName": "张三",
  "mobile": "13800138000",
  "email": "zhangsan@example.com"
}
```

其中 `realName` 为空、`mobile` 不是中国大陆手机号，或 `email` 格式错误时，Controller 的 `create` 方法不会执行。

## 4. 常用注解怎么选

可以把注解分成两类。

第一类是通用约束：

| 需求 | 注解 | 可校验的值 |
| --- | --- | --- |
| 不允许为 `null` | `@NotNull` | 任意对象 |
| 必须是非空白文本 | `@NotBlank` | `CharSequence` |
| 限制长度或元素数 | `@Size` | 文本、集合、Map、数组 |
| 限制数值范围 | `@Min`、`@Max` | `Number` |
| 匹配正则 | `@Pattern` | `CharSequence` |
| 邮箱格式 | `@Email` | `CharSequence` |

例如：

```java
class ProductRequest {
    @NotBlank(message = "商品名不能为空")
    @Size(min = 2, max = 50, message = "商品名长度应为 2 到 50")
    private String name;

    @Min(value = 1, message = "库存不能小于 1")
    @Max(value = 9999, message = "库存不能大于 9999")
    private Integer stock;

    @Pattern(regexp = "[A-Z]{3}-\\d{6}", message = "编码格式应为 ABC-123456")
    private String code;
}
```

第二类是业务格式约束：

| 注解 | 用途 |
| --- | --- |
| `@IdCard` | 中国居民身份证号码 |
| `@MobileNo` | 中国大陆手机号 |
| `@Username` | 用户名 |
| `@Password` | 密码强度 |
| `@Chinese` | 全中文文本 |
| `@Ipv4` | IPv4 地址 |
| `@HttpUrl` | `http` 或 `https` URL |

业务格式约束已内置规则，适合直接用在 DTO 字段或 Controller 参数上。

## 5. 理解 `required`

业务格式约束都有 `required` 属性，默认是 `true`。

```java
class ProfileRequest {
    @IdCard(required = false)
    private String idCard;

    @HttpUrl(required = false)
    private String homepage;
}
```

`required = false` 的意思不是“有值也不校验”，而是：

- `null`、空字符串、全空白字符串：跳过这一项格式校验；
- 只要传入非空内容：仍必须符合对应格式。

所以，“可选的身份证号”应使用 `@IdCard(required = false)`；“必须填写身份证号”直接使用 `@IdCard`。如果你只关心是否为空，请使用 `@NotNull` 或 `@NotBlank`。

## 6. 自定义提示语

有两种方式。

最直接的是在注解上写 `message`：

```java
@IdCard(message = "请填写合法的身份证号码")
private String idCard;
```

如果希望集中管理文案，使用消息键和 YAML：

```java
@IdCard(message = "{id-card}")
private String idCard;
```

```yaml
ajaxjs:
  beanvalidator:
    messages:
      id-card: 请填写合法的身份证号码
      mobile-no: 手机号格式不正确
```

库中已经有默认中文消息；只有需要覆盖时才配置 YAML。

## 7. 校验失败时返回 HTTP 400

请求对象校验失败后，Spring 会抛出 `MethodArgumentNotValidException`，其中包含每个字段的错误信息。可以统一处理：

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
class ValidationErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> handleBodyValidation(
            MethodArgumentNotValidException exception) {
        Map<String, String> result = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors())
            result.put(error.getField(), error.getDefaultMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
```

这样客户端可能收到：

```json
{
  "mobile": "手机号格式不正确",
  "email": "邮箱格式不正确"
}
```

## 8. 校验路径变量

路径变量也能直接使用业务注解，无需额外配置：

```java
import com.ajaxjs.framework.validator.custom.IdCard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@GetMapping("/people/{idCard}")
String find(@PathVariable @IdCard String idCard) {
    return idCard;
}
```

如果路径变量不合法，库会抛出 `ValidatorException`。将它映射成 HTTP 400：

```java
import com.ajaxjs.framework.validator.ValidatorException;

@ExceptionHandler(ValidatorException.class)
ResponseEntity<Map<String, String>> handlePathValidation(ValidatorException exception) {
    return ResponseEntity.badRequest().body(
            java.util.Collections.singletonMap("message", exception.getMessage()));
}
```

`ValidatorConfigurationException` 则表示开发配置有问题，例如在内置注解包中使用了库不认识的注解，或消息配置为空。它通常应该作为服务端错误排查，而不是返回普通的参数错误。

## 9. 添加自己的注解

例如系统有“租户编码必须形如 `TENANT-123`”的规则。先在**应用自己的包**定义注解：

```java
package com.example.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantCode {
    String message() default "租户编码不正确";
}
```

再实现 `ValidatorRule`，并注册为 Spring Bean：

```java
package com.example.validation;

import com.ajaxjs.framework.validator.ValidatorException;
import com.ajaxjs.framework.validator.ValidatorRule;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
class TenantCodeRule implements ValidatorRule {
    @Override
    public boolean supports(Annotation annotation) {
        return annotation.annotationType() == TenantCode.class;
    }

    @Override
    public void validate(Annotation annotation, Object value, String fieldName) {
        TenantCode tenantCode = (TenantCode) annotation;
        if (!(value instanceof String) || !((String) value).matches("TENANT-\\d+"))
            throw new ValidatorException(fieldName + " " + tenantCode.message());
    }
}
```

最后便可使用：

```java
class TenantRequest {
    @TenantCode
    private String tenantCode;
}
```

不要把自己的注解放进 `com.ajaxjs.framework.validator.custom` 包。这个包保留给库内置注解；未知的内置包注解会被当成配置错误。

## 10. 最容易踩的坑

1. 导错包：`@NotBlank` 等必须从 `com.ajaxjs.framework.validator.custom` 导入，而不是 `jakarta.validation.constraints` 或 `javax.validation.constraints`。
2. 忘记加 `@Validated`：请求对象的 Controller 参数缺少它时，字段校验不会按预期触发。
3. 将 `required = false` 理解成完全关闭校验：它只会放过空值，不会放过格式错误的非空值。
4. 在 `@Min` / `@Max` 上使用字符串：这两个注解只支持 `Number`。
5. 把 `ValidatorConfigurationException` 当成用户输入错误：先检查注解是否被支持、`message` 是否正确配置。

至此，你已经可以在 Spring MVC 接口中完成大多数轻量字段校验；复杂的领域规则则通过 `ValidatorRule` 扩展即可。
