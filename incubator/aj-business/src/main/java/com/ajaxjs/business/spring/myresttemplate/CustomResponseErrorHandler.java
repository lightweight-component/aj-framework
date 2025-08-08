package com.ajaxjs.business.spring.myresttemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * @Description: 30X的异常处理
 */
@Slf4j
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = response.getStatusCode();
        if (statusCode.is3xxRedirection())
            return true;

        return super.hasError(response);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = response.getStatusCode();
        if (statusCode.is3xxRedirection()) {
            log.info("########30X错误，需要重定向！##########");
            return;
        }

        super.handleError(response);
    }
}