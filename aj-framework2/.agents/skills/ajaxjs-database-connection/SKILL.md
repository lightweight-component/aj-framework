---
name: ajaxjs-database-connection
description: "在 aj-framework 及使用该框架的项目中，接入、修正或排查 DataBaseConnection 的请求级数据库连接生命周期及 EnableTransaction 事务管理。用于 Controller/Service 的框架连接复用、事务注解放置、异常回滚和请求结束清理；不用于一般 SqlMan CRUD 教程或其他框架的事务配置。"
---

# aj-framework 请求连接与事务管理

## 先确认项目约定

- 阅读项目已有 Controller、Service、依赖版本及框架配置，确认请求连接与事务由谁管理。本 skill 只规范框架连接和事务边界，不规定 CRUD、表结构或数据访问方案。
- 本 skill 属于 `aj-framework2` 项目。项目内副本位于 `.agents/skills/ajaxjs-database-connection/`；从项目根目录查阅 `src/main/java/com/ajaxjs/framework/database/DataBaseConnection.java`、`src/main/java/com/ajaxjs/framework/AutoConfiguration.java`，事务注解位于兄弟模块 `../aj-spring-base/src/main/java/com/ajaxjs/framework/database/EnableTransaction.java`。优先使用这些项目相对路径。
- 当前机器的框架源码入口：`D:/sp42/code/ajaxjs/aj-framework/aj-framework2/src/main/java/com/ajaxjs/framework/database/DataBaseConnection.java`。
- 同目录下的 `../AutoConfiguration.java` 注册连接拦截器；事务注解源码为 `D:/sp42/code/ajaxjs/aj-framework/aj-spring-base/src/main/java/com/ajaxjs/framework/database/EnableTransaction.java`。
- 路径不可用或项目依赖版本不同时，搜索实际依赖的源码/JAR，不据此修改其他项目或升级框架。以项目使用的版本为准。

## 核心约定

1. 正常 HTTP 请求的连接由 `DataBaseConnection.preHandle` 创建并通过 `JdbcConnection` 绑定到当前线程；`afterCompletion` 负责清理及关闭。业务数据访问代码复用这条请求连接，不自行获取数据源、创建或关闭连接。
2. 多条 SQL 需要原子执行时，在实际被 Spring MVC 调用的实现方法上添加 `com.ajaxjs.framework.database.EnableTransaction`。框架开启事务，在请求完成时提交或回滚。不要在 Service 另写事务包装器，也不要手动调用 `setAutoCommit`、`commit`、`rollback`，或叠加另一套 Spring 事务管理。
3. **注解放在实现方法上，不能只标在接口。** 当前源码的请求前处理支持方法注解查找，但请求后处理直接使用 `handlerMethod.getMethod().getAnnotation(EnableTransaction.class)`。例如 Service 实现 Controller 接口时，标注 Service 的 `create/update/delete` 实现方法。
4. 这套事务基于请求拦截器，不是方法 AOP。内部调用带注解的方法不会新开事务；异步线程、定时任务、命令行和直接调用 Service 的测试也不会自动获得请求连接/事务。此类任务需要检查其现有连接上下文，按实际入口设计，不把 HTTP 规则直接套用过去。
5. 需要回滚的异常向上传递。框架会读取 `afterCompletion` 的异常参数；全局异常处理器已接管异常时，读取 `GlobalExceptionHandler.EXCEPTION_HOLDER`。不要捕获异常后仅返回失败值，使框架误判成功并提交。
6. 默认只读方法无需事务注解；确有一致性需求时再添加。`FOR UPDATE` 等锁定查询应位于框架事务内。
7. 若发现 `db.disableAutoConnect=true`、`@IgnoreDataBaseConnect` 或未注册连接拦截器，先确认入口设计；不要直接用手动连接代码掩盖配置差异，也不要在未授权范围更改全局配置。

## 事务边界示例

Controller 定义路由、Service 实现 Controller 时，注解应落在实际执行的实现方法上：

```java
import com.ajaxjs.framework.database.EnableTransaction;

public class ExampleService implements ExampleController {
    @Override
    @EnableTransaction
    public Result save(Input input) {
        // 沿用项目现有数据访问代码；所有 SQL 使用框架绑定的请求连接。
        // 校验、写入、关联更新等需要一起提交或回滚的操作放在此边界内。
        // 失败时向上传递异常，不在这里手动提交、回滚或关闭连接。
        return saveBusinessData(input);
    }
}
```

示例中的业务类型与方法为占位，替换为当前项目实现。SqlMan 只是该框架绑定连接的一种使用者；不要因此把本 skill 扩展成通用 SQL、实体映射、JSON 字段或 CRUD 设计规范。

## 验证与交付

运行项目适用的编译与测试，并确认测试确实执行；父 POM 可能固定跳过测试。不要仅凭 `BUILD SUCCESS` 声称事务验证通过。

涉及连接或事务机制变更时，阅读 [references/testing.md](references/testing.md)，优先验证真实框架拦截器的生命周期。无需仅为普通 SQL 改动搭建新的测试架构。

交付时说明连接/事务归属、注解位置、实际执行的验证及未联调部分。数据库集成测试优先用隔离测试库，不因 skill 本身推断有权修改开发或生产数据。
