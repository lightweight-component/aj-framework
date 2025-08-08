package com.ajaxjs.business.spring.myresttemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class HeadClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        log.info("#####head handle########");
        HttpHeaders headers = httpRequest.getHeaders();
        headers.add("Accept", "application/json");
        headers.add("Accept-Encoding", "gzip");
        headers.add("Content-Encoding", "UTF-8");
        headers.add("Content-Type", "application/json; charset=UTF-8");
        ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, bytes);
        HttpHeaders headersResponse = response.getHeaders();
        headersResponse.add("Accept", "application/json");

        return response;
    }
}