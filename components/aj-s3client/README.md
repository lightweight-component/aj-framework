[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-s3client?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-s3client)
[![Javadoc](https://img.shields.io/badge/javadoc-1.1-brightgreen.svg?)](https://dev.ajaxjs.com/docs/javadoc/aj-s3client/)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)


# A Lightweight S3 Client / S3 存储 Java 客户端

Tutorial: https://blog.csdn.net/zhangxin09/article/details/137671230.

Java Documents: https://dev.ajaxjs.com/docs/javadoc/aj-s3client/.

Support these S3 Storages directly:

- Cloudflare R2
- Scaleway
- Backblaze
- Aliyun OSS
- Netease OSS

## Install 
```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-s3client</artifactId>
    <version>1.1</version>
</dependency>
```


单元测试的配置文件`application.yml`没有随着 VCS 提交，其内容如下：

```yaml
S3Storage:
  Nso:
    accessKey: xx
    accessSecret: xx
    api: nos-eastchina1.126.net
    bucket: xx
  Oss:
    accessKeyId: xx
    secretAccessKey: xx
    endpoint: oss-cn-beijing.aliyuncs.com
    bucket: xx
  LocalStorage: # 本地保存
    absoluteSavePath: c:\temp\ # 若有此值，保存这个绝对路径上
```