package com.ajaxjs.springboot.feign;


import java.io.IOException;

import feign.Request;
import feign.Response;
import feign.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static feign.Util.UTF_8;
import static feign.Util.decodeOrDefault;
import static feign.Util.valuesOrEmpty;

public class FeignLogger extends feign.Logger {
    private final Logger logger;

    public FeignLogger() {
        this(feign.Logger.class);
    }

    public FeignLogger(Class<?> clazz) {
        this(LoggerFactory.getLogger(clazz));
    }

    public FeignLogger(String name) {
        this(LoggerFactory.getLogger(name));
    }

    FeignLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        // Not using SLF4J's support for parameterized messages (even though it would be more efficient)
        // because it would
        // require the incoming message formats to be SLF4J-specific.
        if (logger.isInfoEnabled()) {
            logger.info(String.format(methodTag(configKey) + format, args));
        }
    }

    public static final int SC_NO_CONTENT = 204;

    public static final int SC_RESET_CONTENT = 205;

    /**
     * logRequest方法用于记录请求的日志，包括请求行、请求头和请求体。
     * 首先使用log方法记录请求行，然后根据日志级别，使用log方法记录请求头信息。最后调用logRequestBody方法记录请求体信息。
     */
    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (!logger.isInfoEnabled())
            return;

        log(configKey, "---> %s %s HTTP/1.1", request.httpMethod().name(), request.url());

        if (logLevel.ordinal() >= Level.HEADERS.ordinal()) {
            for (String field : request.headers().keySet()) {
                for (String value : valuesOrEmpty(request.headers(), field))
                    log(configKey, "%s: %s", field, value);
            }
        }

        logRequestBody(configKey, logLevel, request);
    }

    /**
     * logAndRebufferResponse 方法用于记录响应的日志，包括响应行、响应头和响应体。
     * 首先使用log方法记录响应行，然后根据日志级别，使用log方法记录响应头信息。
     * 然后判断响应状态码，如果不是204和205，则读取响应体，并记录响应体的长度和内容。最后返回重新构建的Response对象。
     */
    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {

        String reason = response.reason() != null && logLevel.compareTo(Level.NONE) > 0 ? " " + response.reason() : "";
        int status = response.status();
        log(configKey, "<--- HTTP/1.1 %s%s (%sms)", status, reason, elapsedTime);

        if (logLevel.ordinal() >= Level.HEADERS.ordinal()) {
            for (String field : response.headers().keySet()) {
                for (String value : valuesOrEmpty(response.headers(), field))
                    log(configKey, "%s: %s", field, value);
            }
        }

        int bodyLength = 0;

        if (response.body() != null && !(status == SC_NO_CONTENT || status == SC_RESET_CONTENT)) {
            // HTTP 204 No Content "...response MUST NOT include a message-body"
            // HTTP 205 Reset Content "...response MUST NOT include an entity"
//            if (logLevel.ordinal() >= Level.FULL.ordinal())
//                // CRLF
//                log(configKey, "回参：");

            byte[] bodyData = Util.toByteArray(response.body().asInputStream());
            bodyLength = bodyData.length;

            if (logLevel.ordinal() >= Level.BASIC.ordinal() && bodyLength > 0)
                log(configKey, "回参：%s", decodeOrDefault(bodyData, UTF_8, "Binary data"));

            if (logLevel.ordinal() >= Level.FULL.ordinal())
                log(configKey, "<--- END HTTP (%s-byte body)", bodyLength);

            return response.toBuilder().body(bodyData).build();
        } else
            log(configKey, "<--- END HTTP (%s-byte body)", bodyLength);

        return response;
    }

    private void logRequestBody(String configKey, Level logLevel, Request request) {
        if (!logger.isInfoEnabled())
            return;

        if (request.body() != null) {
            int bodyLength = request.body().length;
            String bodyText = request.charset() != null ? new String(request.body(), request.charset()) : null;

//            if (logLevel.ordinal() >= Level.FULL.ordinal())
//                // CRLF
//                log(configKey, "入参：");

            if (logLevel.ordinal() >= Level.BASIC.ordinal() && bodyLength > 0)
                log(configKey, "入参：%s", bodyText != null ? formatLog(bodyText) : "Binary data");

            if (logLevel.ordinal() >= Level.FULL.ordinal())
                log(configKey, "---> END HTTP (%s-byte body)", bodyLength);
        }
    }

    private static String formatLog(String text) {
        if (text == null)
            return null;

        if (text.contains("\""))
            return text.replace("\"", "");
        else
            return text;
    }
}
