---
title: 数据库访问
subTitle: 2024-12-05 by Frank Cheung
description: 数据库访问
date: 2022-01-05
tags:
  - 数据库访问
layout: layouts/aj-docs.njk
---

# 数据库访问

几乎每个 Web 应用程序都要访问数据库，相当多的日常工作亦是围绕着数据库 CRUD 进行。
许多 Web 框架都有针对数据访问提出自己的解决方案，要么整合 MyBatis/JPA 这类 ORM 或 半 ORM 的框架，要么自研一个 ORM——AJ
Framework 倾向于后者。
但我们更希望实现轻量级的 JDBC 封装与普通 SQL 增强，于是便有了一个名为 SqlMan 的数据库访问组件。
SqlMan 提供便捷的数据访问，首先是类似 Spring JDBCTemplate 的 CRUD 功能，轻松将 SQL 数据库数据获取到 Java Bean 或 Map/List
中。
然后在此基础上提供类似 MyBatis 的 XML 管理 SQL。SqlMan 建议使用经典方式编写原生 SQL 以满足更复杂的业务逻辑。

故而整个数据库操作的内容便交给 SqlMan 处理了，本文就不作过多介绍，大家可以到 SqlMan
官网了解更多 [sqlman.ajaxjs.com](https://sqlman.ajaxjs.com)。

## SQL 参数绑定

在 SQLMan 2.3 中，SQL 的**数据值**必须通过占位符绑定，不能将请求参数直接拼接到 SQL 字符串中。

### 命名参数 `#{}`

当 SQL 执行时传入参数 Map（例如 DataService 接收的查询字符串、表单或 JSON 请求体）时，使用 `#{字段名}`。SQLMan 会将其转换为 JDBC 的 `?` 占位符并绑定对应值：

```sql
SELECT * FROM shop_address WHERE id = #{id}

UPDATE shop_address SET name = #{name} WHERE id = #{id}

INSERT INTO shop_address (name, address) VALUES (#{name}, #{address})
```

例如请求 `GET /address?id=1` 可匹配第一条 SQL；`id` 是数据值，不会被当作 SQL 文本拼接。

### 位置参数 `?`

直接调用 `Action`，或 DataService 将路径变量按顺序传入时，使用 JDBC 位置参数：

```java
new Action("SELECT * FROM shop_address WHERE id = ?").query(id).one();
```

```sql
SELECT * FROM shop_address WHERE id = ?
```

### 不要将请求值放进 `${}`

`${}` 是 SQL 文本替换语法，适合由后端固定或白名单选择的 SQL 标识符，例如表名或列名；它不应用于 URL、表单和 JSON 中传入的值。下面的写法是错误的：

```sql
SELECT * FROM shop_address WHERE id = ${id}
```

数值 `id=1` 会被当成 SQL 标识符而被拒绝。对于普通参数，应改为 `#{id}`。表名、列名、排序字段和排序方向等 SQL 结构也不能作为普通 JDBC 参数绑定，必须在服务端进行固定配置或白名单校验。

### 数据服务组件

数据服务（DataService）的作用是，只需写 SQL 业务逻辑（甚至零代码不写！），即可快速搭建 CRUD 接口服务
——最简单的方式：零代码，在页面上配置好参数，自动生成 SQL 并且直接转化成 HTTP API。大家可以到 DataService
官网了解更多 [dataservice.ajaxjs.com](https://dataservice.ajaxjs.com/cn)。

![](https://dataservice.ajaxjs.com/asset/imgs/ds.jpg)

## 配置数据库连接

### 创建数据源 DataSource

JDBC 标准数据源 DataSource，一个项目至少有一个这个的数据源来完成数据库的连接。可以维护多个 DataSource 数据源。

使用数据源的好处：

- 标准 JDBC 对象，不是直接创建 JDBC Connection，而是从 DataSource 上获取 Connection 对象
- 只要是 DataSource 则已经是带池化的数据源，线程安全，且自动管理连接的关闭。且隐藏了后面连接池的细节
- 当前 AJ Framework 默认使用 Tomcat JDBC Connection Pool 数据源，可配置其他数据源

#### 配置数据库连接

接入了 AJ Framework 包，则自动读取数据库连接的相关配置，自动创建 DataSource，无须手动编码。

在 yaml 中配置：
```yaml
# 数据库连接
db:
    url: jdbc:mysql://xxxx:3306/foo?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    user: root
    psw: xxxxxxxxxxx
```

#### 禁止自动创建数据源
接入了 AJ Framework 包，则通过 Spring Starter 自定加载 DataSource。如果想禁止改特性，可以 yaml 中配置：
`db.isDisableAutoConnect: true` 即可。



## 全局的数据库连接

一般情况下，每个控制器都会产生数据库连接，因为默认每个接口所对应的方法都会涉及数据库操作。这是由框架对 Spring
的`HandlerInterceptor`拦截器默认全局配置的。但是你的控制器方法如果无须数据库连接，可以添加一个注解`@IgnoreDataBaseConnect`
忽略数据库连接，减少资源消耗。
