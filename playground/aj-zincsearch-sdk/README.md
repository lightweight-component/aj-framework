[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-zincsearch-sdk?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-zincsearch-sdk)
[![Javadoc](https://img.shields.io/badge/javadoc-1.0-brightgreen.svg?)](https://dev.ajaxjs.com/docs/javadoc/aj-zincsearch-sdk/)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)


#  ZincSearch Java 客户端

Tutorial: https://blog.csdn.net/zhangxin09/article/details/129337698.

Java Documents: https://dev.ajaxjs.com/docs/javadoc/aj-zincsearch-sdk/.

## Install
```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-zincsearch-sdk</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage

FTP upload:

```java
DocumentService docService = new DocumentService();
docService.setApi("http://localhost:4080");
docService.setUser("admin");
docService.setPassword("Complexpass#123");

Map<String, Object> doc = ObjectHelper.hashMap("title", "AIGC带你看来自“天涯海角”的新种子");
doc.put("content", "央视新闻《开局之年“hui”蓝图》系列微视频，用AI视角，带您看两会。\r\n" + "\r\n"
+ "目前，中国的水果产量稳居世界第一，国人的“果盘子”琳琅满目，瓜果飘香。而作为全球第一的肉类生产和消费大国，近十多年来，全国居民牛羊肉消费量也持续提升。未来的水果产业和牛羊养殖业什么样？让我们跟随AIGC，感受从田间走到舌尖的“新科技”。");

ZincResponse resp = docService.create(target, doc);
assertNotNull(resp);

resp = docService.create(target, doc, 2l);
assertNotNull(resp);
```