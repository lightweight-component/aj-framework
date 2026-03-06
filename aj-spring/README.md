# Spring Enhanced Framework

## The trace for logging

The framework adds a trace to the log for each request by using `TraceXFilter`. Every request will have a unique trace id, so you can use it to trace the request, like this under Linux:


```shell
cat -n info.log |grep "a415ad50dbf84e99b1b56a31aacd209c" # puzzle search
grep -10 'a415ad50dbf84e99b1b56a31aacd209c' info.log     # 10是指上下10行
``` 

Put the logback configuration file `logback.xml` into `src/main/resources/logback.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="10 seconds">
    <!-- 控制台输出 (Console Appender) -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <!-- 设置日志输出格式 -->
            <pattern>[%X{traceId}]%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <!--
       根日志器 (Root Logger) 配置。
       它将应用上面定义的 appender，并设置最低的日志输出级别。
       这里的配置会影响整个应用程序的日志行为。
   -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### Thread Pool
If you want to use thread pool, you can use `ThreadPoolTaskExecutor` or extend `ThreadPoolTaskExecutor`:

```java
@Bean("MyExecutor")
public Executor asyncExecutor() {
    TracedThreadPoolTaskExecutor executor = new TracedThreadPoolTaskExecutor();
    return executor;
}
```

# Thanks to:
- https://mp.weixin.qq.com/s/T6VPeQ6QzG_R95l9dXOAOA
- https://blog.csdn.net/qq_42910468/article/details/154493127