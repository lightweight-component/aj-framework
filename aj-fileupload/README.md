# aj-fileupload

基于 Spring MVC `MultipartFile` 的轻量文件上传与下载组件，提供文件大小限制、扩展名和 Content-Type 校验、文件头及部分容器结构检测、文件命名与本地存储，并支持通过回调接入数据库存储。

组件提供 Java API，不自带上传 Controller、静态资源映射或 Spring Boot 自动配置。应用负责定义 HTTP 接口、读取业务配置和控制访问权限。

## 环境与依赖

使用 Java 17+、Spring Boot 3 / Spring MVC 6 的 Jakarta Servlet 环境；不能直接用于 `javax.servlet` 体系的 Spring Boot 2 应用。

当前模块的 Maven 坐标：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>fileupload</artifactId>
    <version>1.0</version>
</dependency>
```

模块依赖 `com.ajaxjs:base:1.4`，Web 依赖采用 `provided` scope，宿主应用需提供 Spring Web 运行环境，例如 `spring-boot-starter-web`。

## 功能

| 能力 | 当前实现 |
| --- | --- |
| 文件校验 | 配置合法性、非空文件、大小、扩展名、Content-Type、文件头/容器结构 |
| 本地存储 | 自动创建目录，限制相对子目录范围，生成存储名称，返回上传结果 |
| 数据库存储 | 校验后调用应用提供的回调；不内置表结构和持久化实现 |
| 命名策略 | 原名、原名加 UUID、仅 UUID；保留扩展名 |
| 注解配置 | 从方法上的 `@FileUploadAction` 构造配置，可通过回调覆盖 |
| 文件下载 | Spring `ResponseEntity<Resource>` 和 Servlet 响应两种入口 |
| 权限扫描 | POSIX 执行权限日志扫描；不修改权限，不支持 POSIX 的平台跳过 |

每个 `FileUpload` 实例绑定一个文件和一份配置。多文件上传可由 Controller 接收 `MultipartFile[]` 或列表，逐个创建实例处理；组件不提供批量事务、分片上传、断点续传或图片压缩。

## 快速接入

以下示例仅接受不超过 5 MiB 的 JPEG、PNG，使用随机名称保存到 `C:/data/uploads/images`。

```java
import com.ajaxjs.fileupload.DetectType;
import com.ajaxjs.fileupload.FileUpload;
import com.ajaxjs.fileupload.FileUploadConfig;
import com.ajaxjs.fileupload.UploadedResult;
import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads")
public class UploadController {
    @PostMapping(value = "/image", consumes = "multipart/form-data")
    public UploadedResult uploadImage(@RequestParam("file") MultipartFile file) {
        FileUploadConfig config = new FileUploadConfig();
        config.setBaseUploadDir("C:/data/uploads"); // Linux 可设置 /srv/app/uploads
        config.setUploadDir("images");
        config.setMaxFileSize(5);
        config.setAllowExtFilenames(new String[]{"jpg", "jpeg", "png"});
        config.setDetectType(DetectType.IMAGE);
        config.setContentTypePolicy(ContentTypePolicy.Policy.WHITELIST);
        config.setCheckMagicNumber(true);
        config.setNamePolicy(NamePolicy.Policy.RANDOM);
        config.setUrlPrefix("/files/images");
        return new FileUpload(file, config).save();
    }
}
```

请求示例：

```bash
curl -F "file=@avatar.png;type=image/png" http://localhost:8080/uploads/image
```

返回对象的 JSON 形态如下（文件名和大小仅为示意）：

```json
{
  "url": "/files/images/01900000-0000-7000-8000-000000000000.png",
  "fileName": "01900000-0000-7000-8000-000000000000.png",
  "originalFileName": "avatar.png",
  "fileSize": 12345
}
```

`fileSize` 单位为字节，`originalFileName` 保留客户端原值，`fileName` 是实际存储名。`url` 只由 `urlPrefix + fileName` 拼接产生，不代表文件已经可以通过 HTTP 访问。应用需要配置 Nginx、Spring 资源映射或下载接口；设置了 `uploadDir` 时，应同步在 `urlPrefix` 中包含对应的访问路径。URL 拼接不会编码文件名中的空格和中文。

同时配置 Spring Multipart 层的限制，控制请求解析阶段的资源消耗：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 6MB
```

仅执行校验、不保存文件：

```java
new FileUpload(file, config).check();
```

`check()` 不创建存储目录，也不要求数据库保存回调；它仍会检查配置中的必填策略和大小设置。

## 配置项

`FileUploadConfig` 与 `@FileUploadAction` 使用同名配置项：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `storageType` | `LOCAL_DISK` | 本地存储或回调存储 |
| `maxFileSize` | `10` | 单文件上限；按 `1024 × 1024` 字节换算，必须大于 0，等于上限允许上传 |
| `baseUploadDir` | `c:/temp/uploads` | 本地上传根目录，应由应用明确设置 |
| `uploadDir` | 配置对象为 `null`，注解为 `""` | 根目录下的相对子目录；不允许绝对路径、越界或子路径中的符号链接 |
| `allowExtFilenames` | 配置对象为 `null`，注解为空数组 | 扩展名白名单，不含点，比较不区分大小写；空值表示不加此限制 |
| `detectType` | `NONE` | 文件分类，决定内置扩展名、MIME 白名单和内容检测规则 |
| `checkMagicNumber` | `true` | 是否执行文件头或容器检查；`NONE` 不执行格式匹配 |
| `contentTypePolicy` | `ALL` | Content-Type 检查模式 |
| `namePolicy` | `ORIGINAL_RANDOM` | 文件存储名生成方式 |
| `urlPrefix` | `""` | 本地存储返回的访问 URL 前缀，空字符串时返回 `/文件名` |

自定义扩展名白名单与分类扩展名限制同时生效。例如 `detectType=IMAGE` 时，在白名单中加入 `pdf` 也不会允许上传 PDF。即使设置 `NONE`，文件名仍需包含点以提取扩展名，且默认 `ALL` 仍会执行 MIME 映射检查。

### Content-Type 策略

| 策略 | 行为 |
| --- | --- |
| `NO_CHECK` | 跳过 Content-Type 检查 |
| `WHITELIST` | 检查客户端 MIME 是否在所选分类的白名单中；`NONE` 跳过分类检查 |
| `MAPPING` | 使用 `Files.probeContentType` 根据文件名推断 MIME，与客户端 MIME 比较 |
| `ALL` | 同时执行白名单与映射检查 |

映射结果依赖操作系统；无法推断 MIME、缺少客户端 MIME 或类型不匹配时拒绝。映射比较按类型和子类型进行，并兼容部分 WAV MIME 别名。白名单直接匹配 MIME 字符串，因此带额外参数或不同大小写的值可能无法通过。客户端 Content-Type 只作为辅助校验。

### 文件类型检测范围

以下是内容检测层已实现的规则，并不保证每种格式都能通过默认 `ALL` 的 MIME 校验：

| 分类 | 内容检测 |
| --- | --- |
| `NONE` | 不限制内容格式 |
| `IMAGE` | `jpg`、`jpeg`、`png`、`gif`、`webp` 文件头 |
| `OFFICE_FILE` | `doc`、`xls`、`ppt` 的 OLE 头；`pdf`、`rtf` 文件头；OOXML 和 OpenDocument ZIP 结构；`txt`、`md` 跳过魔数匹配 |
| `AUDIO` | `mp3`、`wav`、`flac`、`aac`、`ogg`、`m4a` 文件头 |
| `VIDEO` | `mp4`、`m4v`、`mov` 的 `ftyp`；`mkv`、`webm` 的 EBML DocType；`avi`、`flv` 文件头 |

普通格式最多读取文件前 64 KiB。ZIP 文档采用流式检查：OOXML 检查 `[Content_Types].xml` 与对应的 `word/document.xml`、`xl/workbook.xml` 或 `ppt/presentation.xml`；OpenDocument 检查 `content.xml` 和 `mimetype`。支持 `docx/xlsx/pptx/dotx/xltx/potx/odt/ods/odp`，限制最多 10,000 个 ZIP 条目、100 MiB 累计解压内容，并拒绝包含 `..` 路径段或绝对路径的条目；不会将 ZIP 解压到磁盘。

当前视频检测只接受特定主品牌：MP4 为 `mp42`、M4V 为 `M4V `、MOV 为 `qt  `。其他合法品牌可能被拒绝。音视频分类的扩展名集合大于实际内容检测规则集合，例如 WMA、WMV 等在启用魔数检查时无法通过。文档分类中的 `wps` 没有内容检测规则；`rtf` 虽有文件头规则，但当前文档 MIME 白名单缺少对应类型。请按实际业务格式设置白名单和策略。

这些检测属于格式初筛，没有完整解码文件、验证 XML 内容或扫描恶意代码；旧 Office 的 OLE 头也不能区分 Word、Excel 与 PowerPoint。

### 文件命名

| 策略 | 示例 |
| --- | --- |
| `ORIGINAL` | `avatar.png` |
| `ORIGINAL_RANDOM` | `avatar_UUID.png` |
| `RANDOM` | `UUID.png` |

UUID 使用 `RandomTools.uuidV7()` 生成。命名会剥离原始文件名中的 Unix/Windows 路径并拒绝控制字符、空名称、`.` 和 `..`。`ORIGINAL` 存在同名覆盖风险；对外上传建议使用 `RANDOM`。

## 注解方式

在方法上声明配置，并在方法体中显式调用 `UploadUtils.doUpload`：

```java
import com.ajaxjs.fileupload.*;
import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AnnotatedUploadController {
    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    @FileUploadAction(
        baseUploadDir = "C:/data/uploads",
        uploadDir = "avatars",
        maxFileSize = 5,
        allowExtFilenames = {"jpg", "jpeg", "png"},
        detectType = DetectType.IMAGE,
        contentTypePolicy = ContentTypePolicy.Policy.WHITELIST,
        namePolicy = NamePolicy.Policy.RANDOM,
        urlPrefix = "/files/avatars"
    )
    public UploadedResult upload(@RequestParam("file") MultipartFile file) {
        return UploadUtils.doUpload(AnnotatedUploadController.class, "upload", file);
    }
}
```

支持查找的方法参数签名为 `(MultipartFile)` 或 `(MultipartFile, HttpServletResponse)`。工具只读取目标方法上的直接注解，不会自动拦截请求或调用目标方法，也不读取类级注解。需要额外参数或更灵活的配置时，可直接使用 `FileUploadConfig`。

第四个参数可以在上传前覆盖注解配置，例如将路径替换为应用读取的配置：

```java
return UploadUtils.doUpload(AnnotatedUploadController.class, "upload", file,
    config -> config.setBaseUploadDir("/srv/app/uploads"));
```

也可用 `UploadUtils.fromAnnotation(annotation)` 单独转换配置。

## 数据库存储回调

设置 `StorageType.DATABASE` 后，提供 `BiFunction<MultipartFile, FileUploadConfig, UploadedResult>`。回调在文件校验通过后执行，应用自行完成数据库写入并构造非空结果：

```java
config.setStorageType(com.ajaxjs.fileupload.policy.StorageType.DATABASE);
FileUpload upload = new FileUpload(file, config);
upload.setSaveToDatabase((multipart, uploadConfig) -> {
    // 在这里调用应用的持久化服务，保存文件内容或附件记录。
    // 下面以应用取得的附件标识为例；请替换为真实保存结果。
    String attachmentId = "attachment-id-from-your-service";
    UploadedResult result = new UploadedResult();
    result.setFileName(attachmentId);
    result.setOriginalFileName(multipart.getOriginalFilename());
    result.setFileSize(multipart.getSize());
    result.setUrl("/attachments/" + attachmentId);
    return result;
});
UploadedResult result = upload.save();
```

数据库模式不会自动创建目录、生成存储名或补齐返回字段。未设置回调或回调返回 `null` 会抛出 `IllegalStateException`。注解入口的第五个参数也可以传入此回调。

## 文件下载

接收客户端路径时，使用可信根目录与相对路径的入口：

```java
import com.ajaxjs.fileupload.filedownload.Download;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;

@RestController
public class DownloadController {
    private final Path root = Path.of("C:/data/uploads");

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) {
        // 应用在此检查当前用户是否有权访问该文件。
        return Download.download(root, path, null);
    }
}
```

`Download.download(root, relativePath, downloadName)` 检查绝对路径、目录穿越以及解析后的真实路径是否仍在根目录内。成功返回 200、文件资源、Content-Length、探测的 Content-Type 和 UTF-8 `attachment` 文件名；找不到或不可读文件返回 404，非法路径或读取错误返回 400。`downloadName` 为空时使用文件自身名称。

对于完全由服务端确定的可信路径，还可使用 `Download.download(Path, String)`，或 `Download.downloadServlet(HttpServletResponse, File[, String])`。这些入口没有下载根目录约束，不应直接传入客户端路径。Servlet 入口对不存在的文件返回 404，读取失败抛出 `UncheckedIOException`。

## 错误处理与使用边界

| 异常 | 常见原因 |
| --- | --- |
| `IllegalArgumentException` | 空文件、超限、非法配置、扩展名/MIME 不符、非法文件名或目录 |
| `UnsupportedOperationException` | 文件头/容器不符、未实现的存储类型、注解方法无法找到 |
| `IllegalStateException` | 数据库存储缺少回调或回调返回空结果 |
| `UncheckedIOException` | 读取、目录创建、权限扫描或写入失败 |

组件没有统一 HTTP 异常处理器，应用可使用 `@RestControllerAdvice` 映射错误。`UnsupportedOperationException` 同时用于文件校验失败和不支持的配置，应结合具体调用场景处理。

部署时明确配置上传目录、允许类型和大小限制，并由应用处理身份认证、文件访问授权与必要的内容扫描。上传目录应由应用账户控制，不作为脚本执行目录。内置路径检查仍存在检查与写入之间的竞态窗口；权限扫描只记录日志，不会移除执行权限。原始文件名展示需转义，Windows 特殊文件名等校验仍待完善。

`FILE_SERVICE`、`FILE_SERVICE_API` 目前会明确抛出异常，尚未提供 S3/MinIO 接入。`ShowUrlPolicy.Policy` 未接入配置与执行逻辑，`SendDownload`、`ServletResponse` 为占位 API。其他已记录的问题见 [TO_FIX.md](TO_FIX.md)。

## 源码与测试

| 包/类 | 职责 |
| --- | --- |
| `FileUpload` / `FileUploadConfig` | 单文件处理流程与配置 |
| `FileUploadAction` / `UploadUtils` | 注解配置与上传入口 |
| `UploadedResult` | 上传返回数据 |
| `policy` | 扩展名、Content-Type、命名、URL 与存储枚举 |
| `magicnumber` | 图片、文档、音视频文件头和容器检测 |
| `permission` | POSIX 权限日志扫描 |
| `filedownload.Download` | Spring MVC / Servlet 下载 |

在模块目录执行：

```bash
mvn test
```

现有测试涵盖配置转换、大小边界、路径安全、数据库回调、MIME/扩展名策略、文件头与 ZIP/EBML 容器检测、下载响应头及根目录校验。部分测试使用合成文件头和容器；实际业务仍应使用真实文件样本验证格式兼容性。符号链接测试会在平台无法创建链接时跳过。

## 许可证

[Apache License 2.0](../LICENSE)。
