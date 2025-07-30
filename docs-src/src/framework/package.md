---
title: 打包与部署
subTitle: 2024-12-05 by Frank Cheung
description: 打包与部署
date: 2022-01-05
tags:
  - 打包与部署
layout: layouts/aj-docs.njk
---

# 打包与部署

## 打包

最终打包为 JAR 包。执行 Maven `package` 命令即可。

> ⚠️ 注意：依赖包会拷贝到 `lib` 目录，而不是打包成一个巨大的 Fat Jar。相关插件已在父 POM 引用的 `aj-common-parent` 中集成。

Maven 依赖是一个复杂的过程，很容易导致依赖出问题（版本冲突、Scope定义错误等等）就导致程序起不来，头都大，明明运行 ok
的，这时候你就要检测一下 jar 里面的`MANIFEST.MF`定义，
与正常的文件 text 对比一下，看缺那些包或者版本不对。

## 一键部署

可以通过 Maven 插件一键部署到 SSH 服务器。在 `pom.xml` 中添加 `build` 节点并修改相关配置。

具体参阅： ↗ [Maven 一键部署到 SSH 服务器](https://blog.csdn.net/zhangxin09/article/details/132456075)

```xml

<build>
    <plugins>
        <!-- 一键部署到服务器 SSH -->
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>wagon-maven-plugin</artifactId>
            <version>2.0.2</version>

            <executions>
                <execution>
                    <id>upload-deploy</id>
                    <!-- 运行 install 打包的同时运行 upload-single 和 sshexec -->
                    <phase>install</phase>
                    <goals>
                        <goal>upload-single</goal>
                        <goal>sshexec</goal>
                    </goals>
                    <configuration>
                        <!-- 指定 SSH 账号 -->
                        <serverId>xxx.com</serverId>
                        <!-- 要更新的 jar 包 -->
                        <fromFile>target/xxx.jar</fromFile>
                        <!-- 服务器部署位置 -->
                        <url>${ssh.url.iam}</url>
                        <!-- 重启脚本：先切换目录，才能正确执行脚本 -->
                        <commands>
                            <command>cd /xxx/service/aj-iam; ./startup.sh</command>
                        </commands>
                        <!-- 显示运行命令的输出结果 -->
                        <displayCommandOutputs>true</displayCommandOutputs>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

不知为何不能选择`deploy`阶段，只能选择`package`或`install`。