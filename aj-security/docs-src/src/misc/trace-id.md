---
title: Trace Tracking
subTitle: 2024-12-05 by Frank Cheung
description: Trace Tracking
date: 2022-01-05
tags:
  - Trace Tracking
layout: layouts/docs.njk
---

# Trace Tracking

The integration of Spring Boot with SLF4j can help us easily implement trace tracking functionality, thereby enhancing
the maintainability and diagnosability of systems. Trace tracking enables monitoring of request flows within a system,
helping developers quickly diagnose and resolve issues. In this article, we will introduce how to use Spring Boot and
SLF4j to implement trace tracking.

Without discussing open-source trace tracking systems like Zipkin or SkyWalking, we will manually implement a simple
trace tracking function to enhance our own skills. Since this approach records traces through logs, it can be fully
integrated into your own system. Why do we need trace tracking?

1. **Improve System Maintainability and Diagnosability**: By tracking request flows, we can understand interactions and
   latency between services, enabling developers to quickly diagnose and solve problems.
2. **Optimize System Performance**: Analyzing dependencies and performance issues between services allows optimization
   of call chains and resource utilization, improving system response speed and throughput.
3. **Enhance System Security and Reliability**: Monitoring system operations and anomalies helps promptly identify and
   resolve vulnerabilities and failures, enhancing security and reliability.
4. **Assist in System Design and Development**: Understanding system architecture and processes through trace tracking
   aids in optimizing design and implementation, improving scalability and maintainability.

To uniquely identify each request, users place context information into the MDC (Mapped Diagnostic Context). MDC stores
data via `ThreadLocal`, so there's no need to worry about security issues.

## Implementation

Create a custom `Filter` to obtain or generate a `traceId` when a request arrives.

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
        // Get custom traceId from request header, generate if not present
        String traceId = req.getHeader("x-trace");

        if (!StringUtils.hasLength(traceId))
            traceId = getUUID();

        // Store the current request's traceId in MDC
        MDC.put(TRACE_KEY, traceId);

        log.info("Request: {}", req.getServletPath());
        chain.doFilter(request, response);
    }
}
```

Next, configure the `logback-spring.xml` file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true">
    <contextName>pack-trace</contextName>
    <property name="DEFAULT_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger Line:%-3L - %msg%n"/>
    <!-- Note: traceXId here is the key we stored in MDC, retrieved via %X{traceXId} for unique identification -->
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

With the above log configuration, you can now write a test interface. Accessing the interface will produce the following
console output:

```
2023-12-06 15:18:11.685 INFO  [http-nio-8099-exec-2] com.pack.tracex.test.TraceXFilter Line:40  traceId:【137FEC312666479A98A6433BA80DE951】 - Request: /tracex
2023-12-06 15:18:11.699 INFO  [http-nio-8099-exec-2] com.pack.tracex.test.TraceController Line:28  traceId:【137FEC312666479A98A6433BA80DE951】 - start uri: /tracex
2023-12-06 15:18:11.774 DEBUG [http-nio-8099-exec-2] org.hibernate.SQL Line:135 traceId:【137FEC312666479A98A6433BA80DE951】 - select u1_0.id,u1_0.age,u1_0.email,u1_0.id_no,u1_0.name,u1_0.pwd from users u1_0 where u1_0.id_no=?
2023-12-06 15:18:11.787 INFO  [http-nio-8099-exec-2] com.pack.tracex.test.TraceController Line:31  traceId:【137FEC312666479A98A6433BA80DE951】 - end uri: /tracex
```

The logs output the `traceId`. When debugging issues, we can use this `traceId` for trace tracking.

![](/asset/imgs/trace-id-log.jpg)

We have now completed a very simple trace tracking function. Next, we need to return the generated `traceId` for each
request to the client to assist in troubleshooting.

We can use `ResponseBodyAdvice` to uniformly handle interface data, adding the `traceId` to the returned results.

## Custom ResponseBodyAdvice for Unified Return Value Handling

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

Re-accessing the interface returns the following result:

![](/asset/imgs/trace-id-result.jpg)

The returned result includes the `traceId`. If there are issues with the interface or data that need investigation, we
can use this `traceId` to check the logs.