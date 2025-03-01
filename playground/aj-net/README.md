[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-net?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-net)
[![Javadoc](https://img.shields.io/badge/javadoc-1.4-brightgreen.svg?)](https://dev.ajaxjs.com/docs/javadoc/aj-net/)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)

# A Small HTTP Request Component / 轻量级 HTTP 请求组件

Tutorial: https://framework.ajaxjs.com/docs/aj/?section=net.

Java Documents: https://dev.ajaxjs.com/docs/javadoc/aj-net/.

# Install

Requires Java 1.8+, Maven Snippets:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-net</artifactId>
    <version>1.4</version>
</dependency>
```
# Usage

```java
// 请求百度网站，返回网站的 HTML 内容
String html = Get.get("https://www.baidu.com").toString();

// POST Map 参数
String result = Post.post("http://localhost:8080/post.jsp", new HashMap<String, String>() {
    private static final long serialVersionUID = 1L;
    {
        put("foo", "bar");
    }
});

// POST 字符串参数
result = Post.post("http://localhost:8080/post", "a=1&b=2&c=3");

// api
Post.api(...);
Post.apiXML(...);
```