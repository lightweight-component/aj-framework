# 文件上传组件待修复事项

最后审查日期：2026-07-31

审查范围：`src/main/java/com/ajaxjs/fileupload`

已经完成的修复包括：上传路径穿越、魔数检查的有限读取、ZIP Office
内部标志检查、MKV/WebM `DocType` 识别、下载响应头和路径边界、存储配置
默认值及异常处理，以及文件大小换算溢出。以下是当前剩余问题。

## P0——正确性与安全

- [ ] **让 `ContentTypePolicy.MAPPING` 真正执行校验。**
  `ContentTypePolicy.checkMapping()` 虽然调用了 `Files.probeContentType()`，
  但比较代码已被注释，因此 `MAPPING` 以及 `ALL` 中的映射校验目前会接受
  任何值。需要定义无法识别类型时的行为、规范化媒体类型，并明确上传请求中
  的 `Content-Type` 只能作为不可信的辅助信息，不能作为文件内容证明。

- [ ] **禁止覆盖已有文件并消除本地写入竞争窗口。**
  `FileUpload.saveToDisk()` 先检查目标路径，再调用
  `MultipartFile.transferTo()`。已有文件可能被覆盖，而且目标可以在校验和
  写入之间被替换。应使用 `CREATE_NEW` 原子创建目标文件，或者先写入同目录
  临时文件再原子移动；同时明确 `ORIGINAL` 命名策略发生重名时的行为，并增加
  并发写入和符号链接替换测试。

- [ ] **修复 MP4 魔数检测。**
  `MagicNumberVideo` 调用 `isFtyp(bytes, "mp4")`，但 `isFtyp()` 会读取
  `brand.charAt(3)`，因此符合 MP4 文件头的输入会触发
  `StringIndexOutOfBoundsException`。应识别真实的四字节品牌，例如
  `isom`、`mp41`、`mp42`，并检查 compatible brands，而不是假定一个
  三字符品牌。

- [ ] **统一扩展名白名单与魔数检测器的支持范围。**
  当 `checkMagicNumber=true` 时，扩展名策略宣称支持的格式远多于魔数检测器。
  例如音频中的 `oga`、`wma`、`amr`、`aiff`、`au`、`mid`、`weba`、
  `opus`、`caf`，视频中的 `wmv`、`mpeg`、`mpg`、`3gp`、`3g2`、
  `vob`、`ogv`、`ts`、`f4v`、`rmvb`、`asf`，以及 Office 中的 `wps`。
  这些格式通过扩展名检查后，会因找不到魔数检测器而被拒绝。应为每个对外宣称
  支持的格式实现检测器，或者从白名单中移除暂不支持的格式，并逐项添加测试。

- [ ] **继续加强 ZIP Office 文件识别。**
  当前 DOCX/XLSX/PPTX 检查 `[Content_Types].xml`、`word/document.xml`
  等条目名称，但恶意 ZIP 仍可伪造这些名称。应在限制大小的前提下读取
  `[Content_Types].xml`，关闭 XML 外部实体，并验证对应的 OOXML 内容类型。
  保留条目数量和累计解压量限制，同时测试损坏、截断和伪造标志条目的压缩包。

- [ ] **停用或加固旧对象存储 HTTP 适配器。**
  `OssUpload` 使用明文 `http://` 传输对象；`NsoHttpUpload` 面向已经停止服务
  的平台，并包含硬编码 HTTP 地址。应强制 HTTPS、安全编码对象键、配置连接和
  读取超时，并根据服务商协议验证签名。如果这些适配器已不再受支持，应从生产
  上传路径移除，而不是继续作为可用工具暴露。

## P1——可靠性与 API 行为

- [ ] **重新设计 POSIX 权限检查。**
  目录通常必须具有执行权限才能进入，但 `NoExecFileVisitor` 会把所有具有执行
  权限的目录报告成安全问题。`PermissionCheck` 还使用一个未同步的全局布尔值，
  导致只检查第一个目录。应只检查上传后的普通文件是否意外具有执行位，使用
  线程安全集合按规范化目录分别缓存，并明确发现问题后是阻止上传还是仅记录警告。

- [ ] **使用跨平台的上传目录默认策略。**
  `FileUploadConfig.baseUploadDir` 默认是 `c:/temp/uploads`。在非 Windows
  系统上，它可能变成工作目录下面的意外路径。应要求应用显式配置，或者使用
  跨平台的临时目录或应用数据目录，避免默认写入 Windows 专用路径。

- [ ] **明确 URL 前缀为 null 或空字符串时的行为。**
  当前默认值 `urlPrefix=""` 可以正常使用，但调用方仍可设置为 `null`，
  从而使 `ShowUrlPolicy.concatTwoUrl()` 失败。应明确 `null` 是代表不返回
  URL，还是属于非法配置，并在配置边界校验；同时覆盖 null、空字符串和斜杠组合。

- [ ] **如果 Servlet 下载属于公共 API，为其增加安全根目录入口。**
  `ResponseEntity` 下载已经提供 `root + relativePath` 的安全入口，但
  `downloadServlet(HttpServletResponse, File, ...)` 仍接受任意 `File`。
  应明确记录该方法只能接收服务端可信路径，或者增加等价的安全根目录重载，
  防止控制器代码重新引入任意文件下载。

- [ ] **明确压缩包资源限制。**
  ZIP Office 校验目前固定限制为 10,000 个条目和 100MB 累计解压数据。
  应将其改成配置项或写入公共文档，并说明它与 `maxFileSize` 的关系；增加边界
  测试，避免合法的大型文档被意外拒绝。

## P2——一致性、示例与测试

- [ ] **让扩展名大小写转换不依赖系统语言环境。**
  `FileUploadAction` 和 `FileUploadConfig` 现已统一约定扩展名不带前导点，
  例如 `{"jpg", "png"}`。剩余问题是 `ExtensionCheck` 转换小写时仍使用
  系统默认语言环境，应改用 `Locale.ROOT`。

- [ ] **移除 `FileUploadController` 或将其改为正式示例。**
  它绕过统一上传校验流程、忽略 `mkdirs()` 失败、包含占位域名、手工拼接 URL，
  并直接写入磁盘。应移动到示例或测试目录，或者改为委托 `FileUpload` 完成上传。

- [ ] **替换依赖外部环境的旧上传测试。**
  `TestUpload` 依赖注入的 Spring `WebApplicationContext`、本地资源、已配置的
  控制器以及真实 NOS/OSS 服务，却没有声明为 Spring 集成测试。默认测试套件
  应只保留确定性的单元测试，真实服务检查应移动到显式启用的集成测试，并从
  外部安全提供凭据。

- [ ] **让 Maven 默认生命周期真正执行测试。**
  继承的 Surefire 配置包含 `skipTests=true`，因此 `mvn test` 只编译测试而
  不执行。当前 Maven 又运行在 JDK 26 上，Lombok 需要显式开启注解处理。
  应让 Maven 使用 JDK 17，或者配置 `<proc>full</proc>`/注解处理器路径，并
  确保 CI 实际执行上传和下载回归测试。

- [ ] **删除或实现下载占位方法。**
  `Download.down()` 返回空的 `StreamingResponseBody`，并使用硬编码的表格
  文件名，而且该方法是包级可见。应将其实现为有测试覆盖的正式 API，或者删除，
  避免被误认为可用的下载功能。
