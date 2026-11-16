# 请求连接和事务测试

当任务修改连接或事务机制时，测试应经过实际的 `DataBaseConnection`，而不是在测试中仿写一个提交/回滚包装器来证明 Service 正确。

## 可复用的验证方式

- 使用隔离数据库，例如 H2 MySQL 模式；沿用项目真实的数据访问代码准备数据并触发写入。保持测试入口不加载整个应用，避免启动逻辑连接开发库。
- 为框架建立最小 Spring 上下文，提供测试 `DataSource`，使 `DiContextUtil.getBean(DataSource.class)` 可解析。测试结束恢复原来的静态上下文并关闭测试资源。
- MockMvc 用 `standaloneSetup(controller).addInterceptors(new DataBaseConnection())` 注册真实请求拦截器。测试 Service 实现 Controller 接口时，也验证实际请求路由能够识别实现方法上的 `@EnableTransaction`。
- 测试初始化连接只用于建表和观察已提交数据。发起请求前解除它的线程绑定，让框架为每个请求获取独立连接；不要让初始化连接替代请求连接。测试连接绑定/释放代码属于测试设施，不放回业务 Service。
- 正常写请求：确认框架关闭自动提交，业务数据从独立观察连接可见，请求结束关闭连接并清除线程绑定。正常读请求也应释放连接。
- 写入后抛异常：确认数据回滚，避免只测一个写入前即失败的参数校验。
- 分别覆盖异常未经处理、以及由项目全局异常处理器处理两条路径。后者应验证 `GlobalExceptionHandler.EXCEPTION_HOLDER` 触发回滚，并在请求结束清理。

## 两个测试陷阱

1. H2 的 `setAutoCommit(true)` 可能内部调用 `commit()`。若用真实连接的 Mockito spy，内部调用也会被计入，导致错误地断言框架提交了两次。可使用 `mock(Connection.class, delegatesTo(realConnection))` 仅记录外部调用，同时通过另一个连接检查实际持久化/回滚结果。
2. 直接调用 Service 方法不经过 MVC 拦截器，不能据此断言 `@EnableTransaction` 自动生效。普通业务单元测试可以手动准备测试连接，但不能作为请求事务已验证的证据。

## 适用范围

隔离数据库测试验证的是当前环境下的框架连接及事务行为，不等同于真实数据库并发、连接池及生产身份认证联调。交付时区分实际验证过的范围。

本机已验证的实现示例：`D:/code/wyndme-erp/wyndme-erp-api/src/test/java/cn/wyndme/erp/service/DataDictionaryServiceTest.java`。路径仅用于有该工作区时参考，不是运行 skill 的先决条件。
