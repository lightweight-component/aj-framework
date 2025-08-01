

## BeanWrapper
在 Java 开发中，处理对象的属性操作（如获取、设置、查询等）是一项常见且重要的任务。传统方式通常需要手动编写大量 getter 和 setter 调用代码，
不仅繁琐，而且在处理复杂对象（如嵌套对象、集合属性等）时尤为不便。
Spring 框架的 BeanWrapper 类为这一问题提供了优雅的解决方案。

## Actuator 可以记录 HTTP 日志
通过 /actuator/httptrace 端点，Actuator 可记录最近 100 条 HTTP 请求的元数据（如方法、URI、状态码、耗时等），
但默认不包含请求体、响应体等敏感数据。


Java 7 开始提供了用于解析 MIME 类型的方法 
Files.probeContentType(path)Path path = new File("d:/images/1.png").toPath() ;
String mimeType = Files.probeContentType(path);

该方法利用已安装的 FileTypeDetector 实现来探测 MIME 类型。它调用每个实现的 probeContentType 来解析类型。
如果文件被任何一个实现识别，就会返回内容类型。如果没有，则会调用系统默认的文件类型检测器。

使用Spring的MediaTypeFactoryMediaTypeFactory 是 Spring web模块的一部分，它提供了处理媒体类型的方法。我们将使用它的 getMediaType() 方法根据文件名获取文件的媒体类型。File file = new File("d:/images/1.png");Optional<MediaType> mimeTypeOptional = MediaTypeFactory.getMediaType(file.getName());System.out.println(mimeTypeOptional.isPresent() ? mimeTypeOptional.get() : "未知");
使用该种方式，如果你将文件后缀改成html，那么它返回的将是text/html。所以此种方式不安全。

使用URLConnection方式1：使用 URLConnection 的 getContentType() 方法来检索文件的 MIME 类型：

## 获取请求完整信息
Controller接口获取整个请求的完整信息可以通过HttpEntity对象进行接收，该对象暴露了请求头和请求体。
@RetController@RequestMapping("/users")public class UserController {  
@PostMapping("")  public Object save(HttpEntity<User> entity) {    // 通过HttpEntity可以获取body及header信息。  }}

## 异常处理
很多时候我们都会自定义自己的@RestControllerAdvice类，在该类中定义使用@ExceptionHandler注解的方法进行全局的异常拦截统一处理。这种方式应该是最简单最有效的处理方式了。其实在SpringWeb中提供了一个ResponseEntityExceptionHandler类，该类是个抽象类，该类定义了@ExceptionHandler方法，该异常处理句柄能够处理常见的一些异常，我们可以基础该类，针对这些不同的异常进行特殊的处理。
