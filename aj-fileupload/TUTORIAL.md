# 用 aj-fileupload 给 Spring Boot 项目加一个图片上传接口

头像、商品图片、附件，很多项目都绕不开文件上传。用 Spring 接收文件并不难：Controller 里声明一个 `MultipartFile`，再把它保存到磁盘就行了。不过，真要让用户来上传，还得考虑几件事：文件太大怎么办？能不能只收图片？两个用户都上传了 `avatar.png`，会不会覆盖？文件保存好了，前端该拿什么地址去访问？

`aj-fileupload` 把文件校验、命名和保存这些步骤放在了一起。我们用它写一个图片上传接口，边写边看这些配置有什么用。

## 先把依赖加进来

下面的例子使用 Java 17 和 Spring Boot 3，项目已经引入了 `spring-boot-starter-web`。在 `pom.xml` 中加入组件依赖：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>fileupload</artifactId>
    <version>1.0</version>
</dependency>
```

组件使用的是 `jakarta.servlet`。如果项目还在用 Spring Boot 2，这套代码不能直接照搬。

## 写一个能用的上传接口

先定一个小目标：只收 JPG、JPEG 和 PNG，每个文件最多 5 MiB，保存到本机的 `C:/data/uploads/images` 目录。

新建一个 Controller，放在应用能扫描到的包下：

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

        config.setBaseUploadDir("C:/data/uploads");
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

最后一行负责执行上传：先检查文件，通过后再保存，返回文件名、大小和访问地址。文件为空、超过大小限制或不符合类型要求时，会抛出异常，不会继续保存。

每次请求都创建一个新的 `FileUpload`，因为它绑定的是这一次收到的文件。我们也把配置写在方法里，方便看清楚；实际项目可以从应用配置中读取目录和大小限制。

### 文件到底保存在哪里？

这两行决定磁盘上的位置：

```java
config.setBaseUploadDir("C:/data/uploads");
config.setUploadDir("images");
```

它们拼起来就是 `C:/data/uploads/images`。目录不存在时，组件会尝试创建，但运行应用的账户需要有写入权限。

`uploadDir` 是根目录下的子目录，所以填 `images` 就好，不要填完整路径。`../other` 这样的越界路径会被拒绝。部署到 Linux 时，把根目录换成实际位置，例如 `/srv/app/uploads`。

### 为什么要换掉用户上传的文件名？

假设两个用户都上传了一张叫 `avatar.png` 的图片。如果直接按原名保存到同一个目录，就有覆盖的可能。

例子中使用 `NamePolicy.Policy.RANDOM`，组件会生成 UUID 作为文件名，并保留扩展名。于是，原来的 `avatar.png` 会保存成 `UUID.png`，其中 UUID 是每次生成的标识。

另外两种选择是 `ORIGINAL` 和 `ORIGINAL_RANDOM`：前者使用处理过的原名，后者在原名后面加 UUID，比如 `avatar_UUID.png`。普通用户上传图片时，用 `RANDOM` 通常省事一些，原名仍然能在返回结果中拿到。

## 限制大小，要设置两处

我们在代码里写了 `setMaxFileSize(5)`，组件按 `5 × 1024 × 1024` 字节计算上限，刚好等于上限的文件也可以上传。

但 Spring 在进入这个方法之前，就已经开始解析上传请求了。因此，还要在 `application.yml` 中设置请求大小：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 6MB
```

`max-file-size` 限制单个文件，`max-request-size` 限制整个请求。请求里除了文件还有表单字段和一些封装信息，所以这里给总请求稍微多留了一点空间。

如果以后改成一次上传多张图片，总请求限制也要跟着调整。

## 试着传一张图片

启动应用，准备一张真实的 PNG 图片。假设文件叫 `avatar.png`，应用运行在 8080 端口，可以这样测试：

```bash
curl -F "file=@avatar.png;type=image/png" http://localhost:8080/uploads/image
```

Windows PowerShell 下如果 `curl` 被解析成其他命令，可以改用 `curl.exe`。

也可以使用 Postman：选择 POST，地址填 `/uploads/image`，Body 选 `form-data`，添加名为 `file` 的字段，把字段类型改为 File，再选择图片。

上传成功后，会得到类似这样的结果：

```json
{
  "url": "/files/images/01900000-0000-7000-8000-000000000000.png",
  "fileName": "01900000-0000-7000-8000-000000000000.png",
  "originalFileName": "avatar.png",
  "fileSize": 12345
}
```

`fileName` 对应磁盘上的文件，`originalFileName` 是用户上传时的名字，`fileSize` 是字节数。这个 JSON 中的文件名和大小只是示例。

可以再试两次：上传一个超过 5 MiB 的文件，或者把普通文本文件改名为 `.png` 再上传，看看校验是否按预期拒绝。不要只用文件名判断格式，下一节会解释原因。

## “只允许图片”是怎么检查的？

上传例子中有几行看起来都在检查类型，其实各自检查的东西不同。

`allowExtFilenames` 先看扩展名。我们只填了 `jpg`、`jpeg` 和 `png`，所以 GIF 即使是一张正常图片，也不能通过这个接口。扩展名不需要写前面的点，比较时不区分大小写。

`detectType=IMAGE` 选择了图片这一类的内置规则。自定义扩展名和分类限制需要同时满足；把 `pdf` 加进白名单，也不会让它通过图片分类的检查。

`contentTypePolicy=WHITELIST` 检查请求里声明的文件类型，例如 `image/png`。这个值由客户端提供，可以被改写，所以它只能帮助筛选，不能证明内容就是图片。

`checkMagicNumber=true` 则会读取文件内容，检查 PNG、JPEG 等格式开头的特征字节。这里的“魔数”可以理解成文件格式的标记。把文本文件改成 `.png`，并不会让它拥有 PNG 的文件头。

例子选择 `WHITELIST`，是因为另一种 `MAPPING` 策略要依靠操作系统推断“这个扩展名应该对应什么 MIME 类型”，不同系统的结果可能不同。默认的 `ALL` 会同时执行白名单和映射检查；如果部署后遇到类型不匹配，可以先核对文件扩展名、客户端 Content-Type 和所选策略。

文件头检查也有范围：它不会完整解码图片，更不会判断文件里是否藏有恶意内容。需要处理高风险附件时，还得结合业务自己的扫描或审核流程。

## 上传成功了，为什么图片地址还是 404？

因为保存到磁盘和通过 HTTP 访问，是两件分别要配置的事。

```java
config.setUrlPrefix("/files/images");
```

这行只是告诉组件怎样拼接返回地址。它不会自动给 Spring 注册 `/files/images/**` 路由。

如果这些图片本来就是公开展示的，可以加一个 Spring 静态资源映射：

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;

@Configuration
public class UploadedImageWebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of("C:/data/uploads/images")
            .toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler("/files/images/**")
            .addResourceLocations(location);
    }
}
```

现在 `/files/images/文件名.png` 就能对应到上传目录里的文件。Linux 部署时，这里的目录也要跟上传配置一起调整。

这种映射适合允许公开访问的图片。合同、证件、用户私有附件则更适合走下载接口，在返回文件之前检查访问权限。如果应用配置了 context path，前端使用返回的相对地址时也需要考虑这个前缀。

## 给附件加一个下载入口

组件还提供了下载工具。下面把下载范围限定在 `C:/data/uploads` 下：

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
        // 私有附件需要在这里检查当前用户的访问权限。
        return Download.download(root, path, null);
    }
}
```

请求里的 `path` 填相对路径，例如 `images/上传后返回的文件名.png`。组件会检查路径是否跑到了根目录外，文件不存在时返回 404，非法路径返回 400。

第三个参数决定浏览器下载时显示的名字，填 `null` 就使用实际文件名。如果想显示一个更容易认的名称，可以传入 `"商品图片.png"`，组件会处理 UTF-8 下载文件名。

路径范围检查只能限制文件所在的位置。它并不知道这份附件属于哪个用户，访问权限仍需由业务处理。实际项目里也可以让前端只传附件 ID，由后端查询出对应路径。

## 配置多了，可以换成注解

熟悉前面的配置之后，也可以把它们放在上传方法上：

```java
import com.ajaxjs.fileupload.*;
import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AvatarController {
    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    @FileUploadAction(
        baseUploadDir = "C:/data/uploads",
        uploadDir = "images",
        maxFileSize = 5,
        allowExtFilenames = {"jpg", "jpeg", "png"},
        detectType = DetectType.IMAGE,
        contentTypePolicy = ContentTypePolicy.Policy.WHITELIST,
        namePolicy = NamePolicy.Policy.RANDOM,
        urlPrefix = "/files/images"
    )
    public UploadedResult upload(@RequestParam("file") MultipartFile file) {
        return UploadUtils.doUpload(AvatarController.class, "upload", file);
    }
}
```

注意方法体里的调用仍然要写。`@FileUploadAction` 保存的是配置，`UploadUtils` 根据类名和方法名读取它，再执行上传。

目前这个入口只支持 `(MultipartFile)` 和 `(MultipartFile, HttpServletResponse)` 两种方法参数签名，并且注解要直接写在方法上。若接口还有其他参数，继续使用前面的配置对象方式会更方便。

需要从应用配置中取得目录时，可以在调用中覆盖注解的值：

```java
return UploadUtils.doUpload(AvatarController.class, "upload", file,
    config -> config.setBaseUploadDir("/srv/app/uploads"));
```

## 图片以外的文件怎么处理？

组件还有 `OFFICE_FILE`、`AUDIO` 和 `VIDEO` 分类。例如要收 PDF 和 Word 文档，可以把上传配置中的几行替换为：

```java
config.setDetectType(DetectType.OFFICE_FILE);
config.setAllowExtFilenames(new String[]{"pdf", "doc", "docx"});
config.setContentTypePolicy(ContentTypePolicy.Policy.WHITELIST);
```

DOCX 本身是一个 ZIP 包，因此组件除了看文件头，还会检查包里有没有 Word 文档所需的条目。普通 ZIP 改名为 DOCX，也不能直接通过。旧版 DOC 则检查 OLE 文件头，检查深度与 DOCX 不一样。

音视频需要多留意格式兼容性。目前 MP4 的检测只识别主品牌为 `mp42` 的文件，其他正常 MP4 也可能被拒绝；部分列在分类扩展名集合里的格式，还没有对应的文件头规则。准备支持某种格式时，先用真实样本测试。完整范围可以查 [README](README.md#文件类型检测范围)。

如果只想校验文件，后续交给其他服务处理，可以调用：

```java
new FileUpload(file, config).check();
```

这一步不会保存文件。

数据库存储则通过回调接入：设置 `StorageType.DATABASE`，再用 `setSaveToDatabase(...)` 提供自己的保存逻辑。组件负责先校验，回调负责持久化并返回 `UploadedResult`。它没有内置附件表，也不会自动把文件内容写进数据库。具体写法见 [README 的数据库存储示例](README.md#数据库存储回调)。S3、MinIO 对应的存储枚举目前尚未实现。

## 接到业务里时，还剩几件小事

先处理上传失败的反馈。校验不通过时，组件会抛出异常，前端需要拿到能理解的错误信息。项目已有 `@RestControllerAdvice` 的话，可以接入统一处理；大小、扩展名和 MIME 错误通常是 `IllegalArgumentException`，文件头不匹配使用 `UnsupportedOperationException`，读写失败则是 `UncheckedIOException`。后两类还可能表示配置或服务器问题，不能一律当成用户传错了文件。

再把保存结果和业务记录关联起来。例如用户上传头像后，把文件名或附件 ID 存入用户表，之后查用户详情才能找回这张图片。组件返回上传结果，业务关系需要自己保存。

最后检查实际部署的目录和访问方式。上传账户需要能写文件，但上传目录不应被配置成脚本执行目录。组件里的权限扫描只会记录日志，不会替你修改权限。默认配置也没有限定文件分类，所以接入时应像本文一样明确写出允许的类型。

做到这里，图片上传、保存和访问这条流程就能接上业务了。后续需要调整大小、增加文档类型或换存储方式时，可以回到 [README](README.md) 查具体配置中。
