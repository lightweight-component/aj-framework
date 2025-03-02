[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-json?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-json)
[![Javadoc](https://img.shields.io/badge/javadoc-1.4-brightgreen.svg?)](https://dev.ajaxjs.com/docs/javadoc/aj-json/)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)

# Small JSON parser & serializer 小型 JSON 解释器

小型 JSON 解析器，实现 JSON 与 Map/List 互换，是了解 JSON 解析的好例子。

Tutorial: https://framework.ajaxjs.com/docs/aj/?section=json.

Java Documents: https://dev.ajaxjs.com/docs/javadoc/aj-json/.

# Install
Requires Java 1.8+, Maven Snippets:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-json</artifactId>
    <version>1.4</version>
</dependency>
```

# Usage

```java
// Java to JSON
Object obj = true;
String json = ConvertToJson.toJson(obj);
assertEquals("true", json);

obj = 123;
json = ConvertToJson.toJson(obj);
assertEquals("123", json);

obj = 100000000000000001L;
json = ConvertToJson.toJson(obj);
assertEquals("\"100000000000000001\"", json);

obj = 99999L;
json = ConvertToJson.toJson(obj);
assertEquals("99999", json);

obj = "hello";
json = ConvertToJson.toJson(obj);
assertEquals("\"hello\"", json);

Map map = new HashMap();
map.put("name", "John");
map.put("age", 30);
json = ConvertToJson.toJson(map);
assertEquals("{\"name\":\"John\",\"age\":30}", json);   

// List to JSON
List list = new ArrayList();
list.add(1);
list.add(2);
list.add(3);
String json = ConvertToJson.list2Json(list);
assertEquals("[1, 2, 3]", json);
```