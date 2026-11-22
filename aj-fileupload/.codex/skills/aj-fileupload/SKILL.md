---
name: aj-fileupload
description: "Use AJ File Upload when adding, configuring, troubleshooting, or extending MultipartFile upload validation, local or callback storage, and secure downloads in a Jakarta Servlet Spring MVC application."
---

# AJ File Upload

Use this skill for `com.ajaxjs:fileupload:1.1`. The Java package is `com.ajaxjs.fileupload`, not
`com.ajaxjs.framework.fileupload`. This is a Java API library, not an upload starter: applications must supply their
own controllers, configuration binding, authentication, authorization, and resource mappings.

## Verify compatibility and source

- Use Java 17+ and a Jakarta Servlet environment, such as Spring Boot 3 / Spring MVC 6. Do not mix these APIs with
  `javax.servlet` / Spring Boot 2.
- Add the dependency below; the host application also needs Spring Web, normally `spring-boot-starter-web`.
  The module depends on `com.ajaxjs:base:1.6`.
- Treat the current source and the application's resolved JAR as authoritative. Check actual signatures before
  copying examples, especially when source and published artifacts might differ.
- For details, locate the module's `README.md`, `TUTORIAL.md`, and `src/main/java/com/ajaxjs/fileupload`.
  These paths are relative to the module root, three directories above this skill directory.
  `TO_FIX.md` records known issues and proposals, not guaranteed implemented behavior; verify it against source.

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>fileupload</artifactId>
    <version>1.1</version>
</dependency>
```

## Default workflow: explicit configuration

Create a new `FileUpload` for each file/request. Do not reuse a mutable handler or configuration across requests.
Configure a trusted storage root, relative subdirectory, size limit, allowed extensions, detection type, and random
storage names explicitly. Defaults are not a restrictive upload policy: `detectType` defaults to `NONE`, and the
extension whitelist is empty.

A minimal image-upload controller:

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
    public UploadedResult upload(@RequestParam("file") MultipartFile file) {
        // Authenticate and authorize the caller before saving.
        FileUploadConfig config = new FileUploadConfig();
        config.setBaseUploadDir("/srv/app/uploads"); // Supply from trusted application configuration.
        config.setUploadDir("images");
        config.setMaxFileSize(5);
        config.setAllowExtFilenames(new String[]{"jpg", "jpeg", "png"});
        config.setDetectType(DetectType.IMAGE);
        config.setCheckMagicNumber(true);
        config.setContentTypePolicy(ContentTypePolicy.Policy.WHITELIST);
        config.setNamePolicy(NamePolicy.Policy.RANDOM);
        config.setUrlPrefix("/files/images");
        return new FileUpload(file, config).save();
    }
}
```

`maxFileSize` uses MiB (`1024 * 1024` bytes), must be positive, and permits a file exactly at the limit. Configure
Spring's multipart parser limits separately; library validation runs only after the multipart request is parsed:

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 6MB
```

Use `new FileUpload(file, config).check()` for validation without storage. It still validates configuration and file
rules, but does not create directories or require a database callback. Multiple files require individual handlers;
there is no built-in batch transaction, chunking, resumable upload, or image compression.

## Validation and naming rules

- `allowExtFilenames` contains extensions without dots; comparison is case-insensitive. The custom whitelist and
  `detectType` extension restrictions both apply. Adding `pdf` to an image whitelist does not enable PDF uploads.
- `NONE` skips content-format matching; it is not a safe file policy. Filename extension extraction still requires a
  dot, and the default Content-Type policy `ALL` still checks MIME mapping.
- `WHITELIST` checks client MIME against the selected category; `MAPPING` compares client MIME with
  `Files.probeContentType`; `ALL` does both; `NO_CHECK` skips MIME checks. MIME mapping depends on the OS.
  Do not silently disable checks just to make an incompatible sample pass; inspect actual MIME, extension, and rules.
- Magic-number/container checks are format screening, not malware scanning or full decoding. Use real samples to
  establish support. Audio/video extension lists include formats without implemented content checks; some valid
  MP4 brands are rejected. Read the relevant `magicnumber` implementation before promising format support.
- ZIP Office detection has entry-count and cumulative decompression limits, but still consumes CPU/I/O and does not
  fully validate document content. Isolate or scan untrusted documents as required by the application.
- Prefer `NamePolicy.Policy.RANDOM`. `ORIGINAL_RANDOM` retains part of the client name; `ORIGINAL` risks overwriting
  existing files. Filename cleanup does not cover all Windows special-name cases.

## Result and URL handling

`UploadedResult` exposes `url`, `fileName`, `originalFileName`, and `fileSize` (bytes). Local storage creates the
configured directory and returns the generated storage name. `originalFileName` remains client-supplied; escape it
when displaying it.

`urlPrefix` only constructs a URL string; it does not publish a directory or register a controller. Keep its path
consistent with `uploadDir`, and configure an authorized download endpoint or an explicitly intended static mapping.
Returned URLs do not automatically encode filename path segments. Do not expose private attachments merely because
an upload result contains a URL.

## Optional annotation entry point

`@FileUploadAction` is configuration metadata, not automatic AOP processing. The controller must explicitly call:

```java
return UploadUtils.doUpload(MyUploadController.class, "upload", file);
```

The named method must have a directly declared `@FileUploadAction` and a supported signature: `(MultipartFile)` or
`(MultipartFile, jakarta.servlet.http.HttpServletResponse)`. Class-level annotations are not read by this utility.
It reads configuration and saves the file; it does not invoke the named controller method.

The fourth argument is a `Consumer<FileUploadConfig>` for overriding annotation configuration. The fifth argument is
the database callback. `UploadUtils.fromAnnotation(annotation)` also returns a standalone configuration object.
Prefer explicit `FileUploadConfig` for methods with extra parameters or runtime-specific business configuration.

## Callback storage

For database-managed attachments:

1. Set `config.setStorageType(StorageType.DATABASE)` using `com.ajaxjs.fileupload.policy.StorageType`.
2. Create the handler and call `setSaveToDatabase` with a
   `BiFunction<MultipartFile, FileUploadConfig, UploadedResult>`.
3. Persist through the application's own service and return a non-null `UploadedResult`, filling all needed fields.
4. Call `save()`; it validates the file before invoking the callback.

The library supplies no schema, transaction management, or database persistence implementation. Database mode does
not generate a filename, create directories, or populate the result. Missing callbacks or null results throw
`IllegalStateException`. `FILE_SERVICE` and `FILE_SERVICE_API` are unimplemented; do not advertise built-in S3/MinIO.

## Downloads and security boundaries

Use `com.ajaxjs.fileupload.filedownload.Download`. For any client-controlled path, use the trusted-root overload:

```java
// Check attachment ownership/authorization before returning a response.
return Download.download(java.nio.file.Path.of("/srv/app/uploads"), relativePath, null);
```

This returns `ResponseEntity<Resource>` and checks relative-path containment and resolved real-path containment.
A null download name uses the stored filename. Success returns attachment disposition with UTF-8 filename,
Content-Length, and a probed Content-Type; invalid paths/read errors return 400, missing/unreadable files 404.

`Download.download(Path, String)` and `Download.downloadServlet(HttpServletResponse, File[, String])` have no root
constraint. Use them only for entirely trusted server-selected paths, never arbitrary request paths. The Servlet
entry point returns 404 for missing files and throws `UncheckedIOException` on I/O failure.

Containment checks are not authorization and do not eliminate filesystem check-to-use races. Keep storage writable
only by trusted application accounts, outside script-execution directories. Permission scanning logs warnings; it
does not remove execute permissions or secure the directory. Apply deployment-level restrictions, access control,
quotas, and malware/content scanning where needed. Consider `X-Content-Type-Options: nosniff` for download responses.

Do not use placeholder APIs (`SendDownload`, `ServletResponse`) or assume `ShowUrlPolicy.Policy` is wired into the
upload configuration.

## Error handling and verification

The library does not install an HTTP exception handler. Handle errors through application-specific advice:

- `IllegalArgumentException`: invalid/empty/oversized files, configuration, extension, MIME, or paths.
- `UnsupportedOperationException`: content detection failures, unsupported storage, missing annotation method, or
  some configuration errors. Distinguish user input failures from server configuration failures.
- `IllegalStateException`: missing database callback or a null callback result.
- `UncheckedIOException`: file reading, directory creation, permission scanning, or writing failure; do not expose
  internal filesystem paths to clients.

For integrations, verify a real valid upload, empty/oversized input, extension/MIME/content mismatch, URL/access
behavior, unauthorized access, traversal attempts, and download headers. Test actual supported formats on the target
OS; synthetic magic headers alone do not demonstrate compatibility or safety.

When changing library code, preserve its Jakarta/Java 17 baseline and run `mvn -pl aj-fileupload test` from the
repository root (or `mvn test` inside the module). Add regressions for changed behavior; some symlink tests skip on
platforms unable to create links. A skill-only change does not imply that tests ran or artifacts were published.
