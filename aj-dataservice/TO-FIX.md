# 待修复项

本文记录 `aj-dataservice` 当前已确认、但暂未处理的问题。

## datasource 包

### update 可能假成功

`DatasourceService.update()` 使用 SQLMan 的 `UpdateResult.isOk()`；目标 id 不存在、实际影响 0 行时可能仍返回成功。应改为判断实际影响行数，与逻辑删除保持一致。

### 逻辑删除后的 urlDir 无法复用

`url_dir` 同时受重复检查和数据库唯一索引约束，已逻辑删除的数据源也会占用编码。需要明确这是业务规则；若允许复用，应调整唯一约束和重复检查条件。

### 数据库密码明文保存与日志风险

连接密码存放在 `ds_datasource.password`，SQLMan 的调试日志可能输出参数。列表接口虽已脱敏，但数据库和日志仍有泄露风险；后续应考虑加密存储，并对敏感参数脱敏或关闭相关日志。

### 数据库连接不使用连接池

`JdbcConnection.getConnection(url, username, password)` 每次通过 `DriverManager` 新建连接。适合连通性测试和低频元数据读取，不适合高频请求；高负载场景应由调用应用提供或复用 `DataSource`。

### 元数据接口只支持 MySQL/MariaDB

数据源模型允许配置 H2 等类型，但表/列元数据接口进入 metadata 包后会拒绝非 MySQL/MariaDB。应在 datasource 接口层提前提示支持范围。

### 表元数据分页存在重复关闭连接

`DatasourceService.getTableAndComment()` 外层使用 try-with-resources，内部辅助方法也关闭同一连接。应统一由外层负责关闭，避免连接所有权不清。

### JDBC URL 的访问边界

创建数据源可提交任意 JDBC URL。若管理接口缺少鉴权或 URL 白名单，可能被用于探测内网数据库；应由调用应用限制权限、数据库类型和地址范围。

### crossDb 字段暂未体现实际行为

模型和 DDL 包含 `cross_db`，但当前 datasource 服务未见对应的跨库逻辑，配置含义不明确。

## metadata 包

### 标识符校验尚未完全覆盖

`MySqlProbe` 及 `MetaQuery.getTables(String sql)`、`getTableIndex(String sql)` 仍允许直接传入或拼接 SQL。调用方不得将 HTTP 参数直接传入这些方法；后续应收窄为库名、表名等结构化参数，并统一使用 `BaseMetaQuery` 的标识符校验方法。

### 探针接口需要由应用提供访问控制

`/db_meta` 可返回数据库用户名、路径、运行变量、表结构和 DDL。组件不内置鉴权，生产项目应在网关、Spring Security 或管理端路由层限制管理员访问，并建议默认不对公网暴露。

### 全量元数据读取缺少容量保护

`DataBaseQuery.getDataBaseWithTableFull()` 会枚举所有非系统库、表并逐表读取和解析 DDL。大库场景可能产生较长响应时间和较多数据库查询；后续可增加库/表数量上限、分页、按需读取或缓存。

### DDL 解析仍依赖 JSQLParser 的 MySQL 支持范围

字段注释的常见转义已处理，但 MySQL 特有的新语法、分区、生成列、复杂默认值等仍可能无法被当前 JSQLParser 版本解析。解析失败现在会明确抛出 `MetadataQueryException`；后续可按实际 DDL 补充兼容测试或升级解析器。

### 缺少真实 MySQL/MariaDB 集成测试

现有测试以 H2 夹具和 JDBC mock 为主，能够稳定验证解析和 SQL 拼装，但未覆盖真实 MySQL/MariaDB 驱动与版本差异。后续如需要发布前强校验，可选用 Testcontainers 增加集成测试。

### MySqlProbe 的轻量化设计取舍

`MySqlProbe` 同时承担 Controller、元数据读取、环境读取与 `mysqladmin` 探测职责；`ping()` 仍使用命令行参数传递密码，且依赖本机命令。当前按轻量化定位保留，若将来面向生产运维场景，应再拆分职责并改为带超时的 JDBC 连通性检测。
