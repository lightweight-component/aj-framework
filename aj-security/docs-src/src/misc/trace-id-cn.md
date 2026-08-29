---
title: 链路跟踪
subTitle: 2024-12-05 by Frank Cheung
description: 链路跟踪
date: 2022-01-05
tags:
  - 链路跟踪
layout: layouts/docs-cn.njk
---

# 链路跟踪

SpringBoot 与 SLF4j 的结合可以帮助我们轻松实现链路跟踪功能，从而提高系统的可维护性和可诊断性。
链路跟踪功能能够跟踪系统中的请求链路，帮助开发人员快速诊断和解决问题。
在本文中，我们将介绍如何使用 SpringBoot 和 SLF4j 实现链路跟踪功能。

不谈 Zipkin，SkyWalking 等开源链路跟踪系统，咱们通过自己动手实现一个简单的链路跟踪功能，其实为的就是提高自己。
因为是通过日志的方式记录链路，完全可以用到自己的系统中。为什么需要链路跟踪功能呢？

1. 提高系统的可维护性和可诊断性：通过链路跟踪，可以跟踪系统中的请求链路，了解服务之间的交互和延迟，帮助开发人员快速诊断和解决问题。
1. 优化系统性能：通过链路跟踪，可以分析服务间的依赖关系和性能问题，优化系统的调用链路和资源利用，提高系统的响应速度和吞吐量。
1. 增强系统的安全性和可靠性：通过链路跟踪，可以监控系统的运行状况和异常情况，及时发现和解决系统中的漏洞和故障，增强系统的安全性和可靠性。
1. 辅助系统设计和开发：通过链路跟踪，可以辅助系统设计和开发，了解系统的架构和流程，优化系统的设计和实现，提高系统的可扩展性和可维护性。

为了唯一地标记每个请求，用户将上下文信息放入 MDC (Mapped Diagnostic Context 的缩写)中，MDC 通过`ThreadLocal`
来存储数据，所以不用担心安全问题。

## 动手实现

自定义过滤器`Filter`，该 Filter 的作用是用来在请求到来时获取`traceId`或者生成一个`traceId`值。

```java
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter("/**")
@Component
@Slf4j
public class TraceXFilter implements Filter {
    final static String TRACE_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        // 通过请求header获取自定义的traceId，没有则系统生成
        String traceId = req.getHeader("x-trace");

        if (!StringUtils.hasLength(traceId))
            traceId = getUUID();

        // 将当前请求的traceId存入到MDC中
        MDC.put(TRACE_KEY, traceId);

        log.info("请求: {}", req.getServletPath());
        chain.doFilter(request, response);
    }
}
```

接下来是 logback-spring.xml 日志进行配置。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true">
    <contextName>pack-trace</contextName>
    <property name="DEFAULT_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger Line:%-3L - %msg%n"/>
    <!--注意这里的traceXId就是我们往MDC中存入的key了，通过%X{traceXId}获取唯一标识-->
    <property name="TRACEX_PATTERN"
              value="%green(%d{yyyy-MM-dd HH:mm:ss.SSS}) %highlight(%-5level) [%yellow(%thread)] %logger Line:%-3L traceId:【%red(%X{traceXId})】 - %msg%n"/>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${DEFAULT_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    <appender name="TRACEX" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${TRACEX_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    <logger name="com.pack.tracex.test" additivity="false" level="INFO">
        <appender-ref ref="TRACEX"></appender-ref>
    </logger>
    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

有了上面的日志配置，接下来就可以随意写个接口进行测试。访问接口，控制台输出

```
2023-12-06 15:18:11.685 INFO  [http-nio-8099-exec-2] com.pack.tracex.test.TraceXFilter Line:40  traceId:【137FEC312666479A98A6433BA80DE951】 - 请求: /tracex
2023-12-06 15:18:11.699 INFO  [http-nio-8099-exec-2] com.pack.tracex.test.TraceController Line:28  traceId:【137FEC312666479A98A6433BA80DE951】 - start uri: /tracex
2023-12-06 15:18:11.774 DEBUG [http-nio-8099-exec-2] org.hibernate.SQL Line:135 traceId:【137FEC312666479A98A6433BA80DE951】 - select u1_0.id,u1_0.age,u1_0.email,u1_0.id_no,u1_0.name,u1_0.pwd from users u1_0 where u1_0.id_no=?
2023-12-06 15:18:11.787 INFO  [http-nio-8099-exec-2] com.pack.tracex.test.TraceController Line:31  traceId:【137FEC312666479A98A6433BA80DE951】 - end uri: /tracex
```

日志中输出了 traceId；当程序出现问题需要调试时，我们就可以根据这个 traceId 进行跟踪调试。

![](/asset/imgs/trace-id-log.jpg)

到这我们就完成了一个非常简单的链路跟踪功能。接下来我们需要将每一个请求生成的 traceId 返回到客户端，帮助排查问题。

第二个问题我们可以通过`ResponseBodyAdvice`对接口数据进行统一的处理，将 traceId 统计的添加到返回的结果中。

## 自定义 ResponseBodyAdvice 统一处理返回值

```java

@RestControllerAdvice
public class PackResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().equals(R.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof String) {
            try {
                return this.objectMapper.writeValueAsString(R.success(response, MDC.get(TraceX.TRACE_KEY)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if (body instanceof ResponseEntity<?> entity) {
            return R.success(entity.getBody(), MDC.get(TraceX.TRACE_KEY));
        }
        return R.success(body, MDC.get(TraceX.TRACE_KEY));
    }

}
```

再次访问接口返回如下结果：

![](/asset/imgs/trace-id-result.jpg)

返回的结果包含了`traceId`，如果对接口或者数据有问题需要排查，我们就可以拿着这个`traceId`到日志中进行查看了。

## 参阅

- [从原理到实践:MDC日志链路追踪指南](https://mp.weixin.qq.com/s/AnqZJ7glK7Lib4qJufyVrA)