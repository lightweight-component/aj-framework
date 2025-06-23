[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-ftp?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-ftp)
[![Javadoc](https://img.shields.io/badge/javadoc-1.1-brightgreen.svg?)](https://dev.ajaxjs.com/docs/javadoc/aj-ftp/)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)


# A Lightweight FTP Client

Tutorial: https://blog.csdn.net/zhangxin09/article/details/134222511.

Java Documents: https://dev.ajaxjs.com/docs/javadoc/aj-ftp/.

## Install
```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-net</artifactId>
    <version>1.1</version>
</dependency>
```

## Usage

FTP upload:

```java
UploadFtp client = new UploadFtp("speedtest.tele2.net", 21);
client.login("anonymous", "anonymous");
client.upload("c:\\temp\\re.zip", "/upload/re.zip");
client.closeServer();
```

FTP download:
```java
UploadFtp ftp = new UploadFtp("speedtest.tele2.net", 21);
ftp.login("anonymous", "anonymous");
ftp.getFile("/1KB.zip", "c:\\temp\\re.zip");
```
