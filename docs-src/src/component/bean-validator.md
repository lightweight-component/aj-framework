---
title: Bean 实体校验
subTitle: 2024-12-05 by Frank Cheung
description: Bean 实体校验
date: 2022-01-05
tags:
  - 实体校验
layout: layouts/aj-docs.njk
---


# Bean 实体校验

对于前端的入参，无论是查询的参数还是写入的数据，都需要对其进行合法性的校验，避免数据错误，过滤一些明显不合法的请求。

在 Spring 体系中存在这两种校验机制：

- 基于 JSR-303 标准的实体校验，注解是`@Valid`，有下面两种实现
  - Hibernate Validator，最常见的实现，但是笨重，可达 13mb 的 JAR 包 
  - Apache BVal，比较小众的实现，但轻量级得多，我之前也使用，详见[文章](https://blog.csdn.net/zhangxin09/article/details/50600575)
- Spring 自带的校验器 Validator，注解是`@Validated`

可见 Spring 的更简单和轻量级，但是默认不支持 JSR 注解。怎么结合 JSR-303 注解发挥其作用呢？这正是文本所介绍的组件要解决的问题。 这个组件三个核心类 `ValidatorInitializing`、`ValidatorImpl`、`ValidatorEnum`，去掉注释后总共不超过 200 行源码，实现 10 多 MB 的 Hibernate Validator 的多数功能。

<div class="ref">
    <span class="c">javax.validation</span> 2.0 是 JSR 380 的版本。JSR 380 是 Java 规范请求的缩写，它定义了 Java Bean 验证 API（Java Bean Validation API）。Java Bean 验证 API 提供了一组用于验证对象属性的注解和接口，帮助开发人员进行数据验证和约束。
</div>


组件源码：[https://gitcode.com/lightweight-component/aj-framework/tree/master/aj-framework/src/main/java/com/ajaxjs/framework/validator](https://gitcode.com/lightweight-component/aj-framework/tree/master/aj-framework/src/main/java/com/ajaxjs/framework/validator)。

## 配置方式

首先要在 YAML 配置文件中增加默认的出错提示信息。

```yaml
javax-validation:
  javax.validation.constraints.AssertTrue.message: 值必须为 true
  javax.validation.constraints.AssertFalse.message: 值必须为 false
  javax.validation.constraints.DecimalMax.message: 值不能大于 {value}
  javax.validation.constraints.DecimalMin.message: 值不能小于 {value}
  javax.validation.constraints.Digits.message: 数字值超出范围（应为 [{integer} digits].[{fraction} digits]）
  javax.validation.constraints.Email.message: 值必须为有效的电子邮箱地址
  javax.validation.constraints.Future.message: 值必须为将来的日期
  javax.validation.constraints.FutureOrPresent.message: 值必须为当前或将来的日期
  javax.validation.constraints.Max.message: 值不能大于 {value}
  javax.validation.constraints.Min.message: 值不能小于 {value}
  javax.validation.constraints.Negative.message: 值必须为负数
  javax.validation.constraints.NegativeOrZero.message: 值必须为非正数
  javax.validation.constraints.NotBlank.message: 值不能为空值或空白字符串
  javax.validation.constraints.NotEmpty.message: 值不能为空值、null 或空集合
  javax.validation.constraints.NotNull.message: 值不能为空
  javax.validation.constraints.Null.message: 值必须为空
  javax.validation.constraints.Past.message: 值必须为过去的日期
  javax.validation.constraints.PastOrPresent.message: 值必须为当前或过去的日期
  javax.validation.constraints.Positive.message: 值必须为正数
  javax.validation.constraints.PositiveOrZero.message: 值必须为非负数
  javax.validation.constraints.Pattern.message: 值必须与指定正则表达式匹配
  javax.validation.constraints.Size.message: 大小必须小于 {max}，大于 {min}
```

### 初始化校验组件
引入 aj-framework 即可自动装配该组件。如果单独使用请注入 `ValidatorInitializing`。这是在 Spring 应用程序上下文初始化完成后设置验证器和参数解析器。这个类的作用是在 Spring 启动时，拦截并修改 `RequestMappingHandlerAdapter` 的行为。通过设置自定义的验证器和参数解析器，可以对路径变量进行验证。

```java
@Bean
public ValidatorInitializing initValidator() {
    return new ValidatorInitializing();
}
```

## 使用方法

首先在参数实体属性上添加对应的注解。

```java
import javax.validation.constraints.NotNull;

@Data
public class JvmInfo implements IBaseModel {
    private String name;

    @NotNull
    private String classPath;
    
    // ……
}
```

然后在 controller 里面方法参数上添加 `@Validated` 注解，注意是 `org.springframework.validation.annotation.Validated`。

```java
@PostMapping("/test")
public boolean test(@Validated JvmInfo info) {
    System.out.println(info);
    return true;
}
```

### 路径参数的校验

这是基于 POST 方法提交实体的校验，那么对于路径上的参数是否支持校验呢？答案是支持的。

在 controller 里面方法参数上直接添加你要校验的注解：

```java
@RequestMapping("/test/{mobileNo}/{idNo}")
public Map<String, Object> test(@PathVariable @MobileNo String mobileNo, @PathVariable @IdCard String idNo) { 
    // ……
}
```

便可完成对路径参数的校验了。一般来说既然是路径的参数，那么就是必填非空的了。

值得注意的是，这里的 `@MobileNo`、`@IdCard` 都是自定义的注解，而非标准的 JSR 380 所提供的。这里顺便说说自定义的校验注解的写法。

### 自定义的校验注解

首先定义注解：

```java
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IdCard {
    String message() default "身份证号格式不正确";

    boolean required() default true;
}
```

然后在枚举类 `ValidatorEnum` 中增加具体的校验方法，如果不通过就抛出 `ValidatorException` 异常。

![Bean Validation](../../../asset/aj-docs/bean-v.png)

至此就完成了自定义注解的定义。

## 原理分析

有关原理的分析，请移步至博客文章：↗ [CSDN 博客文章](https://zhangxin.blog.csdn.net/article/details/132255031)。
