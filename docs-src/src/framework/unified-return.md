---
title: 统一返回结果
subTitle: 2024-12-05 by Frank Cheung
description: 统一返回
date: 2022-01-05
tags:
  - 统一返回
  - 解决
layout: layouts/aj-docs.njk
---

# 统一返回结果
开发 REST API 接口时往往需要和前端对接定义返回的数据结构，例如返回下面 JSON 格式的数据。
```json
{
  "success": true,
  "code": null,
  "msg": "操作成功!",
  "data": 具体的数据
}
```

Java 控制器中一般都一个统一返回对象，例如 Spring 自带的`ResponseEntity<T>`，其中泛型`T`是具体返回的数据类型。但是每个控制器方法都返回这个实体，略显有点啰嗦，直接返回数据行不行？也就是写成这样：


```java
@PostMapping("/submit")
boolean jsonSubmit(@RequestBody User user);

@GetMapping("/user")
User User();

@GetMapping("/user_desensitize")
User UserDesensitize();
```

其实可以的——这样代码显示更清爽，减少心智。我们整理一下有哪些情况：

- 如果控制器方法返回的类型是`ResponseEntity<T>`，那么好，很简单，直接处理返回这个对象；
- 如果控制器方法没有返回 `ResponseEntity<T>`，则最终统一返回机制会自动加上。于是你的控制器返回的类型可以直接是 String/int/long/boolean/void/Object/Map/List/Array 等任意类型，当然也包括 Java Bean（POJO）（上述的例子）；
- 如果控制器方法返回的对象实现了接口`IUnifiedReturn`，那么表示这是一个自定义的返回对象，那么也简单，直接处理返回这个对象。这种适合比较特殊的返回结构，数量不是很多的
- 如果数量太多，希望是全局自定义返回对象的，我们也允许。同时可以达到类似第二点的效果，即忽略声明容器类，统一返回机制会自动加上。

下面我们逐个实现。

## 隐式返回

框架的`ResponseResultWrapper`即是统一返回结果封装类。如下控制器写法自动返回。

```java
@PostMapping("/submit")
boolean jsonSubmit(@RequestBody User user);

@GetMapping("/user")
User User();

@GetMapping("/user_desensitize")
User UserDesensitize();
```

框架提供两个特别的注解：

- `@IgnoredGlobalReturn` 忽略全局返回，直接返回业务对象
- `@JsonMessage` 返回 JSON 结构时候，自定义 message 内容

## 自定义返回对象

TODO
