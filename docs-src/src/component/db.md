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
许多 Web 框架都有针对数据访问提出自己的解决方案，要么整合 MyBatis/JPA 这类 ORM 或 半 ORM 的框架，要么自研一个 ORM——AJ Framework 倾向于后者。
但我们更希望实现轻量级的 JDBC 封装与普通 SQL 增强，于是便有了一个名为 SqlMan 的数据库访问组件。
SqlMan 提供便捷的数据访问，首先是类似 Spring JDBCTemplate 的 CRUD 功能，轻松将 SQL 数据库数据获取到 Java Bean 或 Map/List 中。
然后在此基础上提供类似 MyBatis 的 XML 管理 SQL。SqlMan 建议使用经典方式编写原生 SQL 以满足更复杂的业务逻辑。

故而整个数据库操作的内容便交给 SqlMan 处理了，本文就不作过多介绍，大家可以到 SqlMan 官网了解更多 [sqlman.ajaxjs.com](https://sqlman.ajaxjs.com)。

### 数据服务组件

数据服务（DataService）的作用是，只需写 SQL 业务逻辑（甚至零代码不写！），即可快速搭建 CRUD 接口服务 
——最简单的方式：零代码，在页面上配置好参数，自动生成 SQL 并且直接转化成 HTTP API。大家可以到 DataService 官网了解更多 [dataservice.ajaxjs.com](https://dataservice.ajaxjs.com/cn)。

![](https://dataservice.ajaxjs.com/asset/imgs/ds.jpg)
## 配置数据库连接

TODO

## 全局的数据库连接

一般情况下，每个控制器都会产生数据库连接，因为默认每个接口所对应的方法都会涉及数据库操作。这是由框架对 Spring 的`HandlerInterceptor`拦截器默认全局配置的。但是你的控制器方法如果无须数据库连接，可以添加一个注解`@IgnoreDataBaseConnect`忽略数据库连接，减少资源消耗。