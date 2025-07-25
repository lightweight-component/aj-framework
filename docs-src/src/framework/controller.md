---
title: 基于 Java 接口的控制器
subTitle: 2024-12-05 by Frank Cheung
description:  基于 Java 接口的控制器
date: 2022-01-05
tags:
  - 控制器
layout: layouts/aj-docs.njk
---


# 基于 Java 接口的控制器

问题的提出：一般情况下，我们是这样定义一个 Spring Boot/MVC 控制器的：

```java
@PostMapping("/save/{id}")
@ResponseBody
public Book save(@RequestBody Book book, @PathVariable int id) {
    // 调用你的业务类
}
```

一直如此没啥问题。但我们渐渐发现，这个 Controller 里面只有一行调用 Service 的方法……于是我们考虑能不能把这一行代码都省呢？——答案是肯定的！我们就来看看怎么做。

## 把接口作控制器

Spring MVC 5.1 引入了 [新功能](https://github.com/spring-projects/spring-framework/wiki/What%27s-New-in-Spring-Framework-5.x#general-web-revision-1)：

> Controller parameter annotations get detected on interfaces as well: Allowing for complete mapping contracts in controller interfaces.  
> 在接口上的控制器注解也能检测到，并自动映射到实现类上。

说白了就是控制器只是个 Java Interface，对应的实现却不是控制器，而是业务类。这有点控制器与业务类合二为一的味道。实际上为我们减少不少编码量，还是相当值得使用的。这样相当于简化了 Controller 编写。实际上 Spring Cloud 的 OpenFeign 技术也是这套技术，公共定义一套接口，既让消费端使用接口描述 RPC，又让控制器类实现这个接口，异曲同工。

我们用一个用户的例子来看看。首先定义控制器，这里提供了相关的 MVC 的注解。

```java
import com.ajaxjs.user.model.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public interface UserController {
    @GetMapping("/{id}")
    User info(@PathVariable Long id);

    @PostMapping
    Long create(@RequestBody User user);

    /**
     * 检查用户某个值是否已经存在一样的值
     *
     * @param field 字段名，当前只能是 username/email/phone 中的任意一种
     * @param value 字段值，要校验的值
     * @return 是否已经存在一样的值，true 表示存在
     */
    @GetMapping("/checkRepeat")
    Boolean checkRepeat(@RequestParam String field, @RequestParam Object value);

    @PutMapping
    Boolean update(@RequestBody User user);

    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable Long id);
}
```

接着我们实现这个接口，特别地，这是个业务类。当然你也可以说他是控制器类，但这样就失去简化的意义了，只是把之前类上的注解搬到接口身上，并没有简化控制器。我们看看实现类：

```java
import com.ajaxjs.data.CRUD;
import com.ajaxjs.data.entity.CrudUtils;
import com.ajaxjs.framework.entity.BaseEntityConstants;
import com.ajaxjs.sass.SaasUtils;
import com.ajaxjs.user.controller.UserController;
import com.ajaxjs.user.model.User;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
public class UserService implements UserController {
    @Override
    public User info(Long id) {
        String sql = "SELECT * FROM user WHERE stat != 1 AND id = ?";
        sql = SaasUtils.addTenantIdQuery(sql);

        return CRUD.info(User.class, sql, id);
    }

    @Override
    public Long create(@Valid User user) {
        if (checkRepeat("username", user.getUsername()))
            throw new IllegalArgumentException("用户的登录名" + user.getUsername() + "重复");

        return CRUD.create(user);
    }

    @Override
    public Boolean checkRepeat(String field, Object value) {
        String sql = "SELECT * FROM user WHERE stat != 1 AND " + field + " = ?";
        sql = SaasUtils.addTenantIdQuery(sql);
        sql += "LIMIT 1";

        return CRUD.info(sql, value) != null;
    }

    @Override
    public Boolean update(User user) {
        CrudUtils.checkId(user);
        return CRUD.update(user);
    }

    @Override
    public Boolean delete(Long id) {
        // 逻辑删除
        User user = new User();
        user.setId(id);
        user.setStat(BaseEntityConstants.STATUS_DELETED);

        return update(user);
    }
}
```

这有点控制器与业务类合二为一的味道。实际上为我们减少不少编码量，还是相当值得使用的。
