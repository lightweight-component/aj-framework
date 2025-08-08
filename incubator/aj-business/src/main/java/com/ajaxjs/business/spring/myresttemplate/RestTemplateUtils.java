package com.ajaxjs.business.spring.myresttemplate;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class RestTemplateUtils {
    /**
     * 读取时间，自定义默认8s，0表示没有超时时间
     */
    public static final int READ_TIMEOUT = 1000 * 8;
    /**
     * 连接时间，自定义默认8s，0表示没有超时时间
     */
    public static final int CONNEC_TIMEOUT = 1000 * 8;
    /**
     * 重试次数，自定义默认1
     */
    public static final int RETRY_COUNT = 1;

    /**
     * http 请求 GET
     *
     * @param url    地址
     * @param params 参数
     * @return String 类型
     */
    public static String getHttp(String url, JsonNode params) {
        return getHttp(url, params, READ_TIMEOUT, CONNEC_TIMEOUT, RETRY_COUNT);
    }

    /**
     * http 请求 GET
     *
     * @param url           地址
     * @param params        参数
     * @param connecTimeout 连接时间
     * @param readTimeout   读取时间
     * @param retryCount    重试机制
     * @return String 类型
     */
    public static String getHttp(String url, JsonNode params, int connecTimeout, int readTimeout, int retryCount) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connecTimeout);
        requestFactory.setReadTimeout(readTimeout);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8)); // 设置编码集
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler()); // 异常处理
        url = expandURL(url, params);
        String result = null; // 返回值类型;

        for (int i = 1; i <= retryCount; i++) {
            try {
                log.info("【GET/HTTP请求信息】,请求地址:{},请求参数:{}", url, params);
                result = restTemplate.getForObject(url, String.class, params);
                log.info("【GET/HTTP请求信息】,请求地址:{},请求参数:{},返回结果:{}", url, params, result);
                return result;
            } catch (Exception e) {
                log.error("【GET/HTTP请求信息】异常,重试count:{}，请求地址:{},请求参数:{},异常信息:{}", i, url, params, e);
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * https 请求 GET
     *
     * @param url    地址
     * @param params 参数
     * @return String 类型
     */
    public static String getHttps(String url, JsonNode params) {
        String result = getHttps(url, params, READ_TIMEOUT, CONNEC_TIMEOUT, RETRY_COUNT);
        return result;
    }

    /**
     * https 请求 GET
     *
     * @param url           地址
     * @param params        参数
     * @param connecTimeout 连接时间
     * @param readTimeout   读取时间
     * @param retryCount    重试机制
     * @return String 类型
     */
    public static String getHttps(String url, JsonNode params, int connecTimeout, int readTimeout, int retryCount) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connecTimeout);
        requestFactory.setReadTimeout(readTimeout);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8)); // 设置编码集
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler()); //error处理
        restTemplate.setRequestFactory(new HttpsClientRequestFactory()); // 绕过https
        url = expandURL(url, params);
        String result = null; // 返回值类型;

        for (int i = 1; i <= retryCount; i++) {
            try {
                log.info("【GET/HTTPS请求信息】,请求地址:{},请求参数:{}", url, params);
                result = restTemplate.getForObject(url, String.class, params);
                log.info("【GET/HTTPS请求信息】,请求地址:{},请求参数:{},返回结果:{}", url, params, result);
                return result;
            } catch (Exception e) {
                log.error("【GET/HTTPS请求信息】异常,重试count:{}，请求地址:{},请求参数:{},异常信息:{}", i, url, params, e);
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * http 请求 post/JSON
     *
     * @param url    地址
     * @param params 参数
     * @return String 类型
     */
    public static String postHttp(String url, JsonNode params, Map headersMap) {
        String result = postHttp(url, params, headersMap, READ_TIMEOUT, CONNEC_TIMEOUT, RETRY_COUNT);
        return result;
    }

    /**
     * http请求 post/JSON
     *
     * @param url            地址
     * @param params         参数
     * @param headersMap     header
     * @param connectTimeout 连接时间
     * @param readTimeout    读取时间
     * @param retryCount     重试机制
     * @return String 类型
     */
    public static String postHttp(String url, JsonNode params, Map headersMap, int connectTimeout, int readTimeout, int retryCount) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory(); // 时间函数
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        //内部实际实现为 HttpClient
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8)); // 设置编码集
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler()); // 异常处理的headers error 处理
        // 设置·header信息
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAll(headersMap);
        HttpEntity<JsonNode> requestEntity = new HttpEntity<>(params, requestHeaders); // josn utf-8 格式
        String result = null; // 返回值类型;

        for (int i = 1; i <= retryCount; i++) {
            try {
                log.info("【POST/HTTP请求信息】,请求地址:{},请求参数:{}", url, params);
                result = restTemplate.postForObject(url, requestEntity, String.class);
                log.info("【POST/HTTP请求信息】,请求地址:{},请求参数:{},返回结果:{}", url, params, result);
                return result;
            } catch (Exception e) {
                log.error("【POST/HTTP请求信息】异常,重试count:{}，请求地址:{},请求参数:{},异常信息:{}", i, url, params, e);
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * http请求 post/MAP
     *
     * @param url    地址
     * @param params 参数
     * @return String 类型
     */
    public static String postHttp(String url, MultiValueMap params, Map headersMap) {
        String result = postHttp(url, params, headersMap, READ_TIMEOUT, CONNEC_TIMEOUT, RETRY_COUNT);
        return result;
    }

    /**
     * http 普通请求 post/MAP
     *
     * @param url           地址
     * @param params        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
     * @param headersMap    header
     * @param connecTimeout 连接时间
     * @param readTimeout   读取时间
     * @param retryCount    重试机制
     * @return String 类型
     */
    public static String postHttp(String url, MultiValueMap params, Map headersMap, int connecTimeout, int readTimeout, int retryCount) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory(); // 时间函数
        requestFactory.setConnectTimeout(connecTimeout);
        requestFactory.setReadTimeout(readTimeout);
        //内部实际实现为 HttpClient
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8)); // 设置编码集
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler()); // 异常处理的headers error 处理
        // 设置·header信息
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAll(headersMap);
        HttpEntity<Map> requestEntity = new HttpEntity<>(params, requestHeaders); // json utf-8 格式
        String result = null; // 返回值类型;

        for (int i = 1; i <= retryCount; i++) {
            try {
                log.info("【POST/HTTP请求信息】,请求地址:{},请求参数:{}", url, params);
                result = restTemplate.postForObject(url, requestEntity, String.class);
                log.info("【POST/HTTP请求信息】,请求地址:{},请求参数:{},返回结果:{}", url, params, result);
                return result;
            } catch (Exception e) {
                log.error("【POST/HTTP请求信息】异常,重试count:{}，请求地址:{},请求参数:{},异常信息:{}", i, url, params, e);
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * https 普通请求 post/JSON
     *
     * @param url    地址
     * @param params 参数
     * @return String 类型
     */
    public static String postHttps(String url, JsonNode params, Map headersMap) {
        String result = postHttps(url, params, headersMap, READ_TIMEOUT, CONNEC_TIMEOUT, RETRY_COUNT);
        return result;
    }

    /**
     * https 普通请求 post/JSON
     *
     * @param url        请求地址
     * @param params     请求 josn 格式参数
     * @param headersMap headers 头部需要参数
     * @param retryCount 重试机制
     * @return 返回string类型返回值
     */
    public static String postHttps(String url, JsonNode params, Map headersMap, int connecTimeout, int readTimeout, int retryCount) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory(); // 时间函数
        requestFactory.setConnectTimeout(connecTimeout);
        requestFactory.setReadTimeout(readTimeout);
        //内部实际实现为 HttpClient
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8)); // 设置编码集
        restTemplate.setRequestFactory(new HttpsClientRequestFactory()); // 绕过https
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler()); // 异常处理的headers error 处理
        // 设置·header信息
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAll(headersMap);
        HttpEntity<JsonNode> requestEntity = new HttpEntity<>(params, requestHeaders); // json utf-8 格式
        String result = null; // 返回值类型;

        for (int i = 1; i <= retryCount; i++) {
            try {
                log.info("【POST/HTTPS请求信息】,请求地址:{},请求参数:{}", url, params);
                result = restTemplate.postForObject(url, requestEntity, String.class);
                log.info("【POST/HTTPS请求信息】,请求地址:{},请求参数:{},返回结果:{}", url, params, result);
                return result;
            } catch (Exception e) {
                log.error("【POST/HTTPS请求信息】异常,重试count:{}，请求地址:{},请求参数:{},异常信息:{}", i, url, params, e);
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * @param url
     * @param jsonObject
     * @Title: URL拼接
     * @MethodName: expandURL
     * @Return java.lang.String
     * @Exception
     * @Description:
     * @author: 王延飞
     * @date: 2021/1/4 15:30
     */
    private static String expandURL(String url, JsonNode jsonObject) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
//        Set<String> keys = jsonObject
//
//        for (String key : keys)
//            sb.append(key).append("=").append(jsonObject.get(key)).append("&");

        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    /**
     * 出现异常，可自定义
     */
    private static class DefaultResponseErrorHandler implements ResponseErrorHandler {
        /**
         * 对response进行判断，如果是异常情况，返回true
         */
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().value() != HttpServletResponse.SC_OK;
        }

        /**
         * 异常情况时的处理方法
         */
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getBody()));
            StringBuilder sb = new StringBuilder();
            String str;

            while ((str = br.readLine()) != null)
                sb.append(str);

            try {
                throw new Exception(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}